import { getOrderStatus, isApiConfigured, normalizeOrderNumber } from "../shared/restaurant-api.js";
import { setLoading } from "../shared/loading.js";
import { setViewState } from "../shared/view-state.js";
import {
    getOrderStatusConfig,
    ORDER_STATUS_VALUES,
} from "./status-config.js";
import { getCurrentOrder } from "../shared/order-storage.js";

const urlParams = new URLSearchParams(window.location.search);
const storedOrder = getCurrentOrder();
const tableNumber = urlParams.get("mesa") || storedOrder.table || "—";
const requestedOrderNumber = urlParams.get("id") || storedOrder.id;
const orderNumber = requestedOrderNumber ? normalizeOrderNumber(requestedOrderNumber) : "";

class OrderStatus extends HTMLElement {
    connectedCallback() {
        this.orderMesaLabel = this.querySelector("#order-mesa-label");
        this.orderNumberLabel = this.querySelector("#order-number-label");
        this.statusBadge = this.querySelector("#status-badge");
        this.badgeIcon = this.querySelector("#badge-icon");
        this.statusTitulo = this.querySelector("#status-titulo");
        this.statusCard = this.querySelector(".status-card");
        this.statusStepper = this.querySelector("#status-stepper");
        this.kitchenNote = this.querySelector("#status-kitchen-note");
        this.confirmation = this.querySelector("#status-confirmation");
        this.confirmButton = this.querySelector("#confirm-order-ready");
        this.closeNote = this.querySelector("#status-close-note");
        this.confirmed = false;
        this.orderMesaLabel.textContent = tableNumber;
        this.orderNumberLabel.textContent = orderNumber ? orderNumber.padStart(3, "0") : "—";

        this.confirmButton.addEventListener("click", () => this.confirmReady());

        if (isApiConfigured() && orderNumber) {
            this.refreshStatus();
            this.refreshTimer = window.setInterval(() => this.refreshStatus(), 10000);
        } else {
            this.showUnavailable("No hay un pedido identificado para consultar.");
            this.finishLoading();
        }

    }

    finishLoading() {
        setLoading(this, false, "status-loading");
    }

    disconnectedCallback() {
        window.clearInterval(this.refreshTimer);
    }

    showUnavailable(message) {
        this.badgeIcon.textContent = "error";
        this.statusTitulo.textContent = "Estado no disponible";
        this.statusBadge.classList.remove("listo");
        this.statusTitulo.classList.remove("listo");
        this.statusCard.classList.remove("listo");
        setViewState(this.kitchenNote, "error");
        this.kitchenNote.textContent = message;
        this.kitchenNote.hidden = false;
    }

    confirmReady() {
        if (!getOrderStatusConfig(this.dataset.state).isReady || this.confirmed) return;
        this.confirmed = true;
        this.dataset.confirmed = "true";
        this.confirmButton.disabled = true;
        this.confirmation.classList.add("confirmado");
        this.closeNote.hidden = false;
        this.dispatchEvent(new CustomEvent("order-confirmed", {
            bubbles: true,
            detail: { orderId: orderNumber, orderReference: `PE-${orderNumber.padStart(3, "0")}`, tableNumber }
        }));
        window.setTimeout(() => window.close(), 150);
    }

    async refreshStatus() {
        try {
            const order = await getOrderStatus(orderNumber);
            if (!order || !order.estado) {
                this.showUnavailable("El servidor no devolvió el estado del pedido.");
                return;
            }
            this.updateOrderIdentity(order);
            this.setState(order.estado, {
                ordersInPreparation: order.pedidosEnPreparacion,
                queuePosition: order.posicionCola
            });
        } catch (error) {
            this.showUnavailable(error.message || "No fue posible actualizar el estado del pedido.");
            this.dispatchEvent(new CustomEvent("order-status-error", {
                bubbles: true,
                detail: { message: error.message }
            }));
        } finally {
            this.finishLoading();
        }
    }

    updateOrderIdentity(order) {
        if (order.mesa !== undefined && order.mesa !== null) {
            this.orderMesaLabel.textContent = order.mesa;
        }

        if (order.id !== undefined && order.id !== null) {
            const normalizedId = normalizeOrderNumber(order.id);
            this.orderNumberLabel.textContent = normalizedId.padStart(3, "0");
        }
    }

    setState(state, details = {}) {
        const normalizedState = String(state || "").toUpperCase();
        if (!ORDER_STATUS_VALUES.includes(normalizedState)) {
            this.showUnavailable("El servidor devolvió un estado de pedido no reconocido.");
            return;
        }
        const nextState = normalizedState;
        const ordersInPreparation = this.toOptionalNumber(details.ordersInPreparation);
        const queuePosition = this.toOptionalNumber(details.queuePosition);
        const content = getOrderStatusConfig(nextState);
        this.statusStepper.className = `status-stepper estado-${content.visualState}`;
        const isSuccess = content.isReady || content.isCompleted;
        this.statusBadge.classList.toggle("listo", isSuccess);
        this.statusBadge.classList.toggle("error", content.isError);
        this.statusTitulo.classList.toggle("listo", isSuccess);
        this.statusTitulo.classList.toggle("error", content.isError);
        this.statusCard.classList.toggle("listo", isSuccess);
        this.statusCard.classList.toggle("error", content.isError);
        this.badgeIcon.textContent = content.icon;
        const queueCheck = this.querySelector("#check-cola");
        const preparationCheck = this.querySelector("#check-preparandose");
        const finalCheck = this.querySelector("#check-entregado");
        queueCheck.textContent = content.isError ? "close" : "check";
        preparationCheck.textContent = content.isError ? "close" : "check";
        finalCheck.textContent = content.isError ? "close" : "check";
        queueCheck.classList.toggle("estado-error-final", false);
        preparationCheck.classList.toggle("estado-error-final", false);
        finalCheck.classList.toggle("estado-error-final", content.isError);
        this.querySelector("#step-entregado .step-label").textContent = content.isError
            ? "CANCELADO"
            : "ENTREGADO";
        this.statusTitulo.innerHTML = content.title;
        setViewState(this.kitchenNote, null);
        this.kitchenNote.textContent = content.note(ordersInPreparation, queuePosition);
        this.kitchenNote.hidden = false;
        this.confirmation.classList.remove("visible");
        this.confirmation.hidden = !content.isReady || this.confirmed;
        if (content.isReady && !this.confirmed) {
            requestAnimationFrame(() => {
                requestAnimationFrame(() => this.confirmation.classList.add("visible"));
            });
        }
        this.dataset.state = nextState;
        this.dispatchEvent(new CustomEvent("order-status-change", {
            bubbles: true,
            detail: { state: nextState, ordersInPreparation, queuePosition }
        }));
    }

    toOptionalNumber(value) {
        if (value === undefined || value === null || value === "") return null;
        const number = Number(value);
        return Number.isFinite(number) ? number : null;
    }
}

customElements.define("order-status", OrderStatus);
const orderStatus = document.querySelector("order-status");

document.addEventListener("restaurant-order-status", (event) => {
    const detail = event.detail || {};
    orderStatus.setState(detail.estado || detail.state, {
        ordersInPreparation: detail.pedidosEnPreparacion ?? detail.ordersInPreparation,
        queuePosition: detail.posicionCola ?? detail.queuePosition
    });
});

window.orderStatus = orderStatus;
