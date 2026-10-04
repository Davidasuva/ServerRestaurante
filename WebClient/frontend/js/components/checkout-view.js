import { getConfig } from "../config.js";
import { cartStore } from "../store/cart-store.js";
import { esc, formatCOP } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";

/**
 * <checkout-view>: resumen final + método de pago.
 * show() lo muestra; `status = "idle" | "sending" | "sent"` controla los botones.
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

  set status(value) {
    const submit = this.querySelector(".finalizar-checkout");
    if (!submit) return;
    const busy = value !== "idle";
    submit.disabled = busy;
    submit.textContent = { idle: "Enviar pedido a cocina", sending: "Enviando pedido…", sent: "Pedido enviado" }[value];
    this.querySelector(".volver-resumen").disabled = busy;
    this.querySelector(".checkout-payment").disabled = busy;
    this.classList.toggle("pedido-confirmado", busy);
  }
  get isBusy() { return this.querySelector(".finalizar-checkout")?.disabled ?? false; }
}
define("checkout-view", CheckoutView);
