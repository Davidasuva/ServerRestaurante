import { getConfig, isApiConfigured } from "../config.js";
import { getOrderStatus, normalizeOrderNumber } from "../services/restaurant-api.js";
import { esc, readStorage } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";

const STATES = ["EN_COLA", "EN_PREPARACION", "LISTO", "CANCELADO"];

/**
 * <order-status order-id="12" table="4">: seguimiento del pedido (consulta el API cada `statusPollIntervalMs`).
 * Si no se pasan atributos, toma ?id= y ?mesa= de la URL (o sessionStorage).
 * API pública: setState(estado, { ordersInPreparation, queuePosition }).
 * Eventos: "order-status-change", "order-status-error", "order-confirmed".
 */
class OrderStatus extends BaseElement {
  #orderId = "000";
  #table = "";
  #confirmed = false;
  #timer = null;

  connectedCallback() {
    const params = new URLSearchParams(window.location.search);
    this.#table = this.getAttribute("table") || params.get("mesa") || readStorage(sessionStorage, "currentOrderMesa") || "";
    this.#orderId = normalizeOrderNumber(this.getAttribute("order-id") || params.get("id") || readStorage(sessionStorage, "currentOrderId") || "000");
    this.#render();

    this.setState(this.dataset.state || "EN_COLA", {
      ordersInPreparation: Number(this.dataset.ordersInPreparation || 0),
      queuePosition: Number(this.dataset.queuePosition || 1),
    });

    if (isApiConfigured() && this.#orderId !== "000") {
      this.refresh();
      this.#timer = setInterval(() => this.refresh(), getConfig().statusPollIntervalMs);
    }
  }

  disconnectedCallback() { clearInterval(this.#timer); }

  #render() {
    this.innerHTML = `
      <main class="status-main">
        <div class="status-badge" aria-hidden="true"><span class="material-symbols-outlined status-badge-icon">hourglass_top</span></div>
        <h1 class="status-titulo"></h1>
        <section class="status-card" aria-label="Progreso del pedido">
          <div class="status-card-header">
            <div class="status-info-col">
              <span class="status-label">ID PEDIDO</span>
              <strong class="status-value"><span aria-hidden="true">PE-</span><span>${esc(this.#orderId.padStart(3, "0"))}</span></strong>
            </div>
            <div class="status-info-col status-info-right">
              <span class="status-label">MESA</span>
              <strong class="status-value accent">${esc(this.#table)}</strong>
            </div>
          </div>
          <div class="status-stepper estado-en-cola">
            <div class="stepper-track-bg"></div>
            <div class="stepper-track-fill"></div>
            <ol class="stepper-steps">
              <li class="stepper-step"><div class="step-indicator"><div class="step-pulsing-dot" aria-hidden="true"></div><span class="material-symbols-outlined step-check-icon">check</span></div><span class="step-label">EN COLA</span></li>
              <li class="stepper-step"><div class="step-indicator"><div class="step-pulsing-dot" aria-hidden="true"></div><span class="material-symbols-outlined step-check-icon">check</span></div><span class="step-label">EN<br />PREPARACIÓN</span></li>
              <li class="stepper-step"><div class="step-indicator"><span class="material-symbols-outlined step-check-icon">check</span></div><span class="step-label">LISTO</span></li>
            </ol>
          </div>
        </section>
        <p class="status-kitchen-note" role="status" hidden></p>
        <div class="status-confirmation" hidden>
          <button class="status-confirm-button" type="button"><span class="material-symbols-outlined">task_alt</span>Confirmar pedido recibido</button>
          <p class="status-close-note" hidden>Pedido confirmado. Puedes cerrar esta ventana.</p>
        </div>
      </main>`;
    this.badge = this.querySelector(".status-badge");
    this.badgeIcon = this.querySelector(".status-badge-icon");
    this.titleEl = this.querySelector(".status-titulo");
    this.card = this.querySelector(".status-card");
    this.stepper = this.querySelector(".status-stepper");
    this.note = this.querySelector(".status-kitchen-note");
    this.confirmation = this.querySelector(".status-confirmation");
    this.confirmButton = this.querySelector(".status-confirm-button");
    this.closeNote = this.querySelector(".status-close-note");
    this.confirmButton.addEventListener("click", () => this.#confirmReady());
  }

  #confirmReady() {
    if (this.dataset.state !== "LISTO" || this.#confirmed) return;
    this.#confirmed = true;
    this.dataset.confirmed = "true";
    this.confirmButton.disabled = true;
    this.confirmation.classList.add("confirmado");
    this.closeNote.hidden = false;
    this.emit("order-confirmed", { orderId: this.#orderId, orderReference: `PE-${this.#orderId.padStart(3, "0")}`, tableNumber: this.#table });
    setTimeout(() => window.close(), 150);
  }

  async refresh() {
    try {
      const order = await getOrderStatus(this.#orderId);
      if (order) this.setState(order.estado, { ordersInPreparation: order.pedidosEnPreparacion, queuePosition: order.posicionCola });
    } catch (error) {
      this.emit("order-status-error", { message: error.message });
    }
  }

  setState(state, details = {}) {
    const upper = String(state).toUpperCase();
    const next = STATES.includes(upper) ? upper : "EN_COLA";
    const ordersInPreparation = Number(details.ordersInPreparation || 0);
    const queuePosition = Number(details.queuePosition || 1);
    const ready = next === "LISTO";

    this.stepper.className = `status-stepper estado-${next.toLowerCase().replace("_", "-")}`;
    for (const el of [this.badge, this.titleEl, this.card]) el.classList.toggle("listo", ready);
    this.badgeIcon.textContent = { EN_COLA: "hourglass_top", EN_PREPARACION: "skillet", LISTO: "check_circle", CANCELADO: "cancel" }[next];

    const copy = {
      EN_COLA: ["¡Tu pedido está<br />en cola!", `Hay ${ordersInPreparation} pedido${ordersInPreparation === 1 ? "" : "s"} en preparación. Posición en cola: ${queuePosition}.`],
      EN_PREPARACION: ["¡Tu pedido está en<br />preparación!", "La cocina está preparando tu pedido."],
      LISTO: ["¡Tu pedido está listo!", "Puedes recogerlo o disfrutarlo en tu mesa."],
      CANCELADO: ["Tu pedido fue<br />cancelado", "Acércate al mostrador y el personal te ayudará."],
    }[next];
    this.titleEl.innerHTML = copy[0]; // texto estático propio, sin datos del servidor
    this.note.textContent = copy[1];
    this.note.hidden = false;

    this.confirmation.classList.remove("visible");
    this.confirmation.hidden = !ready || this.#confirmed;
    if (ready && !this.#confirmed) requestAnimationFrame(() => requestAnimationFrame(() => this.confirmation.classList.add("visible")));

    this.dataset.state = next;
    if (next === "CANCELADO") clearInterval(this.#timer); // estado final: no hace falta seguir consultando
    this.emit("order-status-change", { state: next, ordersInPreparation, queuePosition });
  }
}
define("order-status", OrderStatus);
