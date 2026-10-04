import { getOrderStatus, isApiConfigured, normalizeOrderNumber } from "../shared/restaurant-api.js";
import { setLoading } from "../shared/loading.js";
import { setViewState } from "../shared/view-state.js";
import {
    getInitialOrderStatus,
    getOrderStatusConfig,
    ORDER_STATUS_VALUES,
} from "./status-config.js";
import { getCurrentOrder } from "../shared/order-storage.js";

const urlParams = new URLSearchParams(window.location.search);
const storedOrder = getCurrentOrder();
const tableNumber = urlParams.get("mesa") || storedOrder.table || "4";
const orderNumber = normalizeOrderNumber(urlParams.get("id") || storedOrder.id || "000");

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
        this.orderNumberLabel.textContent = orderNumber.padStart(3, "0");

        this.confirmButton.addEventListener("click", () => this.confirmReady());

        const ordersInPreparation = Number(this.dataset.ordersInPreparation || 0);
        const requestedState = this.dataset.state || "EN_COLA";
        const initialState = getInitialOrderStatus(requestedState, ordersInPreparation);
        this.setState(initialState, {
            ordersInPreparation,
            queuePosition: Number(this.dataset.queuePosition || 1)
        });

        if (isApiConfigured() && orderNumber) {
            this.refreshStatus();
            this.refreshTimer = window.setInterval(() => this.refreshStatus(), 10000);
        } else {
            this.finishLoading();
        }

    }

    finishLoading() {
        setLoading(this, false, "status-loading");
    }

    disconnectedCallback() {
        window.clearInterval(this.refreshTimer);
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
            if (!order) return;
            this.setState(order.estado, {
                ordersInPreparation: order.pedidosEnPreparacion,
                queuePosition: order.posicionCola
            });
        } catch (error) {
            setViewState(this.kitchenNote, "error");
            this.kitchenNote.textContent = error.message || "No fue posible actualizar el estado del pedido.";
            this.kitchenNote.hidden = false;
            this.dispatchEvent(new CustomEvent("order-status-error", {
                bubbles: true,
                detail: { message: error.message }
            }));
        } finally {
            this.finishLoading();
        }
    }

    setState(state, details = {}) {
        const normalizedState = String(state).toUpperCase();
        const nextState = ORDER_STATUS_VALUES.includes(normalizedState) ? normalizedState : "EN_COLA";
        const ordersInPreparation = Number(details.ordersInPreparation || 0);
        const queuePosition = Number(details.queuePosition || 1);
        this.statusStepper.className = `status-stepper estado-${nextState.toLowerCase().replace("_", "-")}`;
        const content = getOrderStatusConfig(nextState);
        this.statusBadge.classList.toggle("listo", content.isReady);
        this.statusTitulo.classList.toggle("listo", content.isReady);
        this.statusCard.classList.toggle("listo", content.isReady);
        this.badgeIcon.textContent = content.icon;
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
