import { getConfig, isApiConfigured } from "../config.js";
import { getOrderStatus, normalizeOrderNumber } from "../services/restaurant-api.js";
import { getOrderStatusConfig, ORDER_STATUS_VALUES } from "../services/order-status-config.js";
import { esc, readStorage } from "../utils/format.js";
import { demoLoadingDelay, setLoading } from "../utils/loading.js";
import { setViewState } from "../utils/view-state.js";
import { BaseElement, define } from "./base-element.js";


/**
 * <order-status order-id="12" table="4">: seguimiento del pedido (consulta el API cada `statusPollIntervalMs`).
 * Si no se pasan atributos, toma ?id= y ?mesa= de la URL (o sessionStorage).
 * Estados: PENDIENTE, EN_COLA, PREPARANDOSE, PREPARADO, ENTREGADO, CANCELADO (ver services/order-status-config.js).
 * API pública: setState(estado, { ordersInPreparation, queuePosition }).
 * Eventos: "order-status-change", "order-status-error", "order-confirmed".
 */
class OrderStatus extends BaseElement {
  #orderId = "000";
  #table = "";
  #confirmed = false;
  #timer = null;
  #refreshing = false; // evita consultas superpuestas si el servidor tarda más que el intervalo de refresco
  #loaded = false;

  connectedCallback() {
    setLoading(this, true, "status-loading"); // esqueleto hasta la primera respuesta del servidor
    const params = new URLSearchParams(window.location.search);
    this.#table = this.getAttribute("table") || params.get("mesa") || readStorage(sessionStorage, "currentOrderMesa") || "";
    this.#orderId = normalizeOrderNumber(this.getAttribute("order-id") || params.get("id") || readStorage(sessionStorage, "currentOrderId") || "000");
    this.#render();

    this.setState(this.dataset.state || "EN_COLA", {
      ordersInPreparation: this.dataset.ordersInPreparation,
      queuePosition: this.dataset.queuePosition,
    });

    if (isApiConfigured() && this.#orderId !== "000") {
      this.refresh();
      this.#timer = setInterval(() => this.refresh(), getConfig().statusPollIntervalMs);
    } else {
      this.#finishLoading();
    }
  }

  /** Quita el esqueleto (solo la primera vez; las consultas siguientes actualizan la pantalla ya visible). */
  #finishLoading() {
    if (this.#loaded) return;
    this.#loaded = true;
    window.setTimeout(() => setLoading(this, false, "status-loading"), demoLoadingDelay());
  }

  disconnectedCallback() { clearInterval(this.#timer); }

  #render() {
    this.innerHTML = `
      <main class="status-main">
        <div class="status-skeleton" aria-hidden="true">
          <div class="status-skeleton-badge skeleton"></div>
          <div class="status-skeleton-title skeleton"></div>
          <div class="status-skeleton-card">
            <div class="status-skeleton-card-header">
              <div class="status-skeleton-field">
                <span class="status-skeleton-label skeleton"></span>
                <span class="status-skeleton-value skeleton"></span>
              </div>
              <div class="status-skeleton-field status-skeleton-field-right">
                <span class="status-skeleton-label skeleton"></span>
                <span class="status-skeleton-value skeleton"></span>
              </div>
            </div>
            <div class="status-skeleton-stepper">
              <span class="status-skeleton-step skeleton"></span>
              <span class="status-skeleton-step skeleton"></span>
              <span class="status-skeleton-step skeleton"></span>
            </div>
            <div class="status-skeleton-step-labels">
              <span class="status-skeleton-label skeleton"></span>
              <span class="status-skeleton-label skeleton"></span>
              <span class="status-skeleton-label skeleton"></span>
            </div>
          </div>
        </div>
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
              <li class="stepper-step" id="step-cola">
                <div class="step-indicator"><div class="step-pulsing-dot" id="dot-cola" aria-hidden="true"></div><span class="material-symbols-outlined step-check-icon" id="check-cola">check</span></div>
                <span class="step-label">EN COLA</span>
              </li>
              <li class="stepper-step" id="step-preparandose">
                <div class="step-indicator"><div class="step-pulsing-dot" id="dot-preparacion" aria-hidden="true"></div><span class="material-symbols-outlined step-check-icon" id="check-preparandose">check</span></div>
                <span class="step-label">EN<br />PREPARACIÓN</span>
              </li>
              <li class="stepper-step" id="step-entregado">
                <div class="step-indicator"><span class="material-symbols-outlined step-check-icon" id="check-entregado">check</span></div>
                <span class="step-label">ENTREGADO</span>
              </li>
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
    if (!getOrderStatusConfig(this.dataset.state).isReady || this.#confirmed) return;
    this.#confirmed = true;
    this.dataset.confirmed = "true";
    this.confirmButton.disabled = true;
    this.confirmation.classList.add("confirmado");
    this.closeNote.hidden = false;
    this.emit("order-confirmed", { orderId: this.#orderId, orderReference: `PE-${this.#orderId.padStart(3, "0")}`, tableNumber: this.#table });
    setTimeout(() => window.close(), 150);
  }

  async refresh() {
    if (this.#refreshing) return;
    this.#refreshing = true;
    try {
      const order = await getOrderStatus(this.#orderId);
      if (!order) return;
      if (!order.estado) { this.#showUnavailable("El servidor devolvió un estado de pedido no reconocido."); return; }
      this.setState(order.estado, { ordersInPreparation: order.pedidosEnPreparacion, queuePosition: order.posicionCola });
    } catch (error) {
      this.#showUnavailable(error.message || "No fue posible actualizar el estado del pedido.");
      this.emit("order-status-error", { message: error.message });
    } finally {
      this.#refreshing = false;
      this.#finishLoading();
    }
  }

  /** Aviso de error en la nota inferior; conserva el último estado mostrado (se reintenta en la siguiente consulta). */
  #showUnavailable(message) {
    setViewState(this.note, "error");
    this.note.textContent = message;
    this.note.hidden = false;
  }

  setState(state, details = {}) {
    const upper = String(state || "").toUpperCase();
    if (!ORDER_STATUS_VALUES.includes(upper)) { this.#showUnavailable("El servidor devolvió un estado de pedido no reconocido."); return; }
    const next = upper;
    const content = getOrderStatusConfig(next);
    const num = (v) => (v === undefined || v === null || v === "" || !Number.isFinite(Number(v)) ? null : Number(v));
    const ordersInPreparation = num(details.ordersInPreparation);
    const queuePosition = num(details.queuePosition);
    const success = content.isReady || content.isCompleted;

    this.stepper.className = `status-stepper estado-${content.visualState}`;
    for (const el of [this.badge, this.titleEl, this.card]) {
      el.classList.toggle("listo", success);
      el.classList.toggle("error", content.isError);
    }
    this.badgeIcon.textContent = content.icon;
    // Cancelado: los tres pasos muestran una X y el último pasa a llamarse CANCELADO
    for (const id of ["cola", "preparandose", "entregado"]) {
      const check = this.querySelector(`#check-${id}`);
      check.textContent = content.isError ? "close" : "check";
    }
    this.querySelector("#step-entregado .step-label").textContent = content.isError ? "CANCELADO" : "ENTREGADO";
    this.titleEl.innerHTML = content.title; // texto estático propio, sin datos del servidor
    setViewState(this.note, null); // quita el aviso de error de una consulta anterior fallida
    this.note.textContent = content.note(ordersInPreparation, queuePosition);
    this.note.hidden = false;

    this.confirmation.classList.remove("visible");
    this.confirmation.hidden = !content.isReady || this.#confirmed;
    if (content.isReady && !this.#confirmed) requestAnimationFrame(() => requestAnimationFrame(() => this.confirmation.classList.add("visible")));

    this.dataset.state = next;
    if (next === "ENTREGADO" || next === "CANCELADO") clearInterval(this.#timer); // estados finales: no hace falta seguir consultando
    this.emit("order-status-change", { state: next, ordersInPreparation, queuePosition });
  }
}
define("order-status", OrderStatus);
