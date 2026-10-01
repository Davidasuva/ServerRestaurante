import { getOrderStatus, isApiConfigured } from "./restaurant-api.js";

const urlParams = new URLSearchParams(window.location.search);
const tableNumber = urlParams.get("mesa") || sessionStorage.getItem("currentOrderMesa") || "4";
const orderId = urlParams.get("id") || sessionStorage.getItem("currentOrderId") || "PE001";

class OrderStatus extends HTMLElement {
    connectedCallback() {
        this.orderMesaLabel = this.querySelector("#order-mesa-label");
        this.orderIdLabel = this.querySelector("#order-id-label");
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
        this.orderIdLabel.textContent = orderId;

        this.confirmButton.addEventListener("click", () => this.confirmReady());

        const ordersInPreparation = Number(this.dataset.ordersInPreparation || 0);
        const requestedState = this.dataset.state || "EN_COLA";
        const initialState = requestedState === "EN_PREPARACION" && ordersInPreparation > 0
            ? "EN_COLA"
            : requestedState;
        this.setState(initialState, {
            ordersInPreparation,
            queuePosition: Number(this.dataset.queuePosition || 1)
        });

        if (isApiConfigured() && orderId) {
            this.refreshStatus();
            this.refreshTimer = window.setInterval(() => this.refreshStatus(), 10000);
        }

    }

    disconnectedCallback() {
        window.clearInterval(this.refreshTimer);
    }

    confirmReady() {
        if (this.dataset.state !== "LISTO" || this.confirmed) return;
        this.confirmed = true;
        this.dataset.confirmed = "true";
        this.confirmButton.disabled = true;
        this.confirmation.classList.add("confirmado");
        this.closeNote.hidden = false;
        this.dispatchEvent(new CustomEvent("order-confirmed", {
            bubbles: true,
            detail: { orderId, tableNumber }
        }));
        window.setTimeout(() => window.close(), 150);
    }

    async refreshStatus() {
        try {
            const order = await getOrderStatus(orderId);
            if (!order) return;
            this.setState(order.estado, {
                ordersInPreparation: order.pedidosEnPreparacion,
                queuePosition: order.posicionCola
            });
        } catch (error) {
            this.dispatchEvent(new CustomEvent("order-status-error", {
                bubbles: true,
                detail: { message: error.message }
            }));
        }
    }

    setState(state, details = {}) {
        const normalizedState = String(state).toUpperCase();
        const validStates = ["EN_COLA", "EN_PREPARACION", "LISTO"];
        const nextState = validStates.includes(normalizedState) ? normalizedState : "EN_COLA";
        const ordersInPreparation = Number(details.ordersInPreparation || 0);
        const queuePosition = Number(details.queuePosition || 1);
        this.statusStepper.className = `status-stepper estado-${nextState.toLowerCase().replace("_", "-")}`;
        this.statusBadge.classList.toggle("listo", nextState === "LISTO");
        this.statusTitulo.classList.toggle("listo", nextState === "LISTO");
        this.statusCard.classList.toggle("listo", nextState === "LISTO");
        this.badgeIcon.textContent = { EN_COLA: "hourglass_top", EN_PREPARACION: "skillet", LISTO: "check_circle" }[nextState];

        const content = {
            EN_COLA: { title: "¡Tu pedido está<br />en cola!", note: `Hay ${ordersInPreparation} pedido${ordersInPreparation === 1 ? "" : "s"} en preparación.` },
            EN_PREPARACION: { title: "¡Tu pedido está en<br />preparación!", note: "La cocina está preparando tu pedido." },
            LISTO: { title: "¡Tu pedido está listo!", note: "Puedes recogerlo o disfrutarlo en tu mesa." }
        }[nextState];
        this.statusTitulo.innerHTML = content.title;
        this.kitchenNote.textContent = nextState === "EN_COLA"
            ? `${content.note} Posición en cola: ${queuePosition}.`
            : content.note;
        this.kitchenNote.hidden = false;
        this.confirmation.classList.remove("visible");
        this.confirmation.hidden = nextState !== "LISTO" || this.confirmed;
        if (nextState === "LISTO" && !this.confirmed) {
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
