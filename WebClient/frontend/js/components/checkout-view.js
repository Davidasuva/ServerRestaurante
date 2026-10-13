import { getConfig } from "../config.js";
import { cartStore } from "../store/cart-store.js";
import { esc, formatCOP } from "../utils/format.js";
import { setViewState } from "../utils/view-state.js";
import { BaseElement, define } from "./base-element.js";

/** Cómo se ve la vista en cada fase del envío (el color lo pone el CSS según `estado-*`). */
const STATUS_VIEW = {
  idle:    { state: null,      icon: "check_circle",      title: "Tu pedido está listo",         label: "Enviar pedido a cocina", busy: false },
  sending: { state: "loading", icon: "progress_activity", title: "Enviando tu pedido a cocina…", label: "Enviando pedido…",       busy: true },
  sent:    { state: "success", icon: "check_circle",      title: "¡Pedido enviado a cocina!",    label: "Pedido enviado",         busy: true },
  error:   { state: "error",   icon: "error",             title: "No pudimos enviar tu pedido",  label: "Enviar pedido a cocina", busy: false },
};
const STATUS_HINT = {
  sending: "Esto puede tardar unos segundos. No cierres ni recargues esta pantalla.",
};

/**
 * <checkout-view>: resumen final + método de pago.
 * show() lo muestra; `status = "idle" | "sending" | "sent" | "error"` controla los botones y la pantalla de carga.
 * showError(mensaje) deja la vista en estado "error" explicando qué pasó.
 * Emite "checkout-submit" { method } y "checkout-back".
 */
class CheckoutView extends BaseElement {
  connectedCallback() {
    this.classList.add("checkout-vista");
    this.addEventListener("click", (event) => {
      if (event.target.closest(".finalizar-checkout")) this.emit("checkout-submit", { method: this.method });
      else if (event.target.closest(".volver-resumen")) this.emit("checkout-back");
    });
    this.hidden = true;
  }

  get method() { return this.querySelector('input[name="metodo-pago"]:checked')?.value; }

  show() {
    const methods = getConfig().paymentMethods;
    this.innerHTML = `
      <div class="checkout-status" role="status">
        <span class="material-symbols-outlined">check_circle</span>
        <strong>Tu pedido está listo</strong>
      </div>
      <p class="checkout-hint" hidden></p>
      <section class="checkout-order" aria-labelledby="checkout-order-title">
        <div class="checkout-section-heading"><h2 id="checkout-order-title">Tu pedido</h2></div>
        <div class="checkout-lineas">${cartStore.items.map((item) => `
          <div class="checkout-linea">
            <img src="${esc(item.image)}" alt="">
            <span class="checkout-linea-info"><strong>${esc(item.name)}</strong><small>${item.quantity} unidad${item.quantity === 1 ? "" : "es"}</small></span>
            <strong class="checkout-linea-precio">${formatCOP(cartStore.itemTotal(item))}</strong>
          </div>`).join("")}
        </div>
        <div class="checkout-totales"><div class="checkout-total"><span>Total</span><strong>${formatCOP(cartStore.total)}</strong></div></div>
      </section>
      <fieldset class="checkout-payment">
        <legend>Selecciona el método de pago</legend>
        ${methods.map((m, i) => `<label><input type="radio" name="metodo-pago" value="${esc(m)}" ${i === 0 ? "checked" : ""} /><span>${esc(m)}</span></label>`).join("")}
      </fieldset>
      <div class="checkout-actions">
        <button class="finalizar-checkout" type="button">Enviar pedido a cocina</button>
        <button class="volver-resumen" type="button">Volver al resumen</button>
      </div>`;
    this.status = "idle";
    this.hidden = false;
  }

  set status(value) { this.#apply(value); }

  showError(message) { this.#apply("error", message); }

  #apply(value, message) {
    const submit = this.querySelector(".finalizar-checkout");
    if (!submit) return;
    const view = STATUS_VIEW[value] ?? STATUS_VIEW.idle;
    setViewState(this, view.state);
    this.querySelector(".checkout-status .material-symbols-outlined").textContent = view.icon;
    this.querySelector(".checkout-status strong").textContent = view.title;
    const hint = this.querySelector(".checkout-hint");
    hint.textContent = message ?? STATUS_HINT[value] ?? "";
    hint.hidden = !hint.textContent;
    submit.disabled = view.busy;
    submit.textContent = view.label;
    this.querySelector(".volver-resumen").disabled = view.busy;
    this.querySelector(".checkout-payment").disabled = view.busy;
    this.classList.toggle("pedido-confirmado", view.busy);
  }
  get isBusy() { return this.querySelector(".finalizar-checkout")?.disabled ?? false; }
}
define("checkout-view", CheckoutView);
