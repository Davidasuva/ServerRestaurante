import { cartStore } from "../store/cart-store.js";
import { buildOrderPayload } from "../services/mappers.js";
import { normalizeOrderNumber, submitOrder } from "../services/restaurant-api.js";
import { formatCOP, writeStorage } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";
import { showToast } from "./app-toast.js";
import "./order-list.js";
import "./checkout-view.js";
import "./confirm-dialog.js";

/**
 * <order-panel table="3" status-url="status.html">: hoja inferior con resumen, checkout y envío del pedido.
 * Se abre con el evento de documento "order-panel-open". Emite "order-panel-toggle" en document y
 * "restaurant-order-submit" (payload) justo antes de llamar al API.
 */
class OrderPanel extends BaseElement {
  #mode = "summary"; // "summary" | "checkout"
  #wasEmpty = true;
  #drag = { active: false, startY: 0, distance: 0 };
  #refresh = () => this.#update();
  #open = () => this.open();
  #onKey = (event) => {
    if (event.key !== "Escape" || !this.isOpen) return;
    if (this.dialog.isOpen) this.dialog.dismiss(); else this.close();
  };
  #docDrag = {
    mousemove: (e) => this.#moveDrag(e.clientY),
    mouseup: () => this.#endDrag(),
    touchmove: (e) => { if (this.#drag.active) this.#moveDrag(e.touches[0].clientY); },
    touchend: () => this.#endDrag(),
    touchcancel: () => this.#endDrag(),
  };

  connectedCallback() {
    this.innerHTML = `
      <div class="pedido-overlay" aria-hidden="true">
        <section class="panel-pedido" id="panel-pedido" role="dialog" aria-modal="true" aria-labelledby="titulo-pedido">
          <button class="arrastre-pedido" type="button" aria-label="Deslizar para cerrar detalles del pedido"><span></span></button>
          <header class="panel-pedido-header"><h1 id="titulo-pedido">Resumen y Pago</h1></header>
          <order-list></order-list>
          <div class="pedido-vacio">
            <div class="pedido-vacio-icono"><span class="material-symbols-outlined">shopping_bag</span></div>
            <h2>Tu pedido está vacío</h2>
            <p>Agrega algunos platos del menú para comenzar.</p>
            <button class="volver-menu" type="button">Volver al Menú</button>
          </div>
          <checkout-view></checkout-view>
          <confirm-dialog></confirm-dialog>
          <footer class="panel-pedido-footer">
            <div class="total-panel"><span>Total a pagar</span><strong>$0</strong></div>
            <button class="confirmar-pedido" type="button" disabled>Confirmar Pedido</button>
          </footer>
        </section>
      </div>`;
    this.overlay = this.querySelector(".pedido-overlay");
    this.panel = this.querySelector(".panel-pedido");
    this.list = this.querySelector("order-list");
    this.checkout = this.querySelector("checkout-view");
    this.dialog = this.querySelector("confirm-dialog");
    this.emptyState = this.querySelector(".pedido-vacio");
    this.footer = this.querySelector(".panel-pedido-footer");
    this.confirmButton = this.querySelector(".confirmar-pedido");

    this.overlay.addEventListener("click", (e) => { if (e.target === this.overlay) this.close(); });
    this.querySelector(".volver-menu").addEventListener("click", () => { if (this.list.ensureSaved()) this.close(); });
    this.confirmButton.addEventListener("click", () => { if (!this.confirmButton.disabled && this.list.ensureSaved()) this.#showCheckout(); });
    this.checkout.addEventListener("checkout-submit", (e) => { e.stopPropagation(); this.#submit(e.detail.method); });
    this.checkout.addEventListener("checkout-back", async (e) => {
      e.stopPropagation();
      const ok = await this.dialog.ask({
        title: "¿Volver al resumen?", message: "¿Deseas regresar para modificar los productos de tu pedido?",
        confirmLabel: "Sí, volver", cancelLabel: "Cancelar",
      });
      if (ok && !this.checkout.isBusy) this.#showSummary();
    });
    this.#bindDrag();

    cartStore.addEventListener("change", this.#refresh);
    document.addEventListener("order-panel-open", this.#open);
    document.addEventListener("keydown", this.#onKey);
    this.#update();
  }

  disconnectedCallback() {
    cartStore.removeEventListener("change", this.#refresh);
    document.removeEventListener("order-panel-open", this.#open);
    document.removeEventListener("keydown", this.#onKey);
    for (const [name, fn] of Object.entries(this.#docDrag)) document.removeEventListener(name, fn);
  }

  get isOpen() { return this.overlay.classList.contains("abierto"); }

  open() { this.#setOpen(true); }
  close() {
    if (this.checkout.isBusy) return; // enviando el pedido: la pantalla de carga no se puede cerrar
    this.#setOpen(false);
  }

  #setOpen(open) {
    if (open) this.panel.style.transform = "";
    this.overlay.classList.toggle("abierto", open);
    this.overlay.setAttribute("aria-hidden", String(!open));
    document.body.classList.toggle("pedido-abierto", open);
    document.dispatchEvent(new CustomEvent("order-panel-toggle", { detail: { open } }));
  }

  /* ---------- vistas ---------- */

  #showCheckout() {
    this.#mode = "checkout";
    this.checkout.show();
    this.#update();
    showToast("Pedido confirmado", "success");
  }

  #showSummary() {
    this.#mode = "summary";
    this.checkout.hidden = true;
    this.#update();
  }

  #update() {
    const empty = cartStore.items.length === 0;
    const summary = this.#mode === "summary";
    this.querySelector(".total-panel strong").textContent = formatCOP(cartStore.total);
    this.confirmButton.disabled = empty;
    this.list.hidden = !summary || empty;
    this.emptyState.hidden = !summary || !empty;
    this.footer.hidden = !summary;
    if (summary && empty && !this.#wasEmpty) this.#animateEmptyState();
    this.#wasEmpty = empty;
  }

  #animateEmptyState() {
    this.emptyState.classList.add("preparando-entrada");
    requestAnimationFrame(() => requestAnimationFrame(() => this.emptyState.classList.remove("preparando-entrada")));
  }

  /* ---------- envío ---------- */

  async #submit(method) {
    const tableId = this.getAttribute("table") || "1";
    const payload = buildOrderPayload({ items: cartStore.items, tableId, method, total: cartStore.total });
    document.dispatchEvent(new CustomEvent("restaurant-order-submit", { detail: payload }));
    this.checkout.status = "sending";
    try {
      const order = await submitOrder(payload);
      const number = normalizeOrderNumber(order.id ?? "000");
      cartStore.lock();
      this.checkout.status = "sent";
      writeStorage(sessionStorage, "currentOrderId", number);
      writeStorage(sessionStorage, "currentOrderMesa", tableId);
      showToast(`Pedido enviado a cocina · ${method}`, "success");
      this.emit("order-submitted", { order, payload });
      const url = `${this.getAttribute("status-url") || "status.html"}?mesa=${encodeURIComponent(tableId)}&id=${encodeURIComponent(number)}`;
      setTimeout(() => { window.location.href = url; }, 1600);
    } catch (error) {
      this.checkout.showError(error.message);
      showToast(error.message, "error");
    }
  }

  /* ---------- arrastrar para cerrar ---------- */

  #bindDrag() {
    const handle = this.querySelector(".arrastre-pedido");
    handle.addEventListener("mousedown", (e) => { e.preventDefault(); this.#startDrag(e.clientY); });
    handle.addEventListener("touchstart", (e) => { e.preventDefault(); this.#startDrag(e.touches[0].clientY); }, { passive: false });
    document.addEventListener("mousemove", this.#docDrag.mousemove);
    document.addEventListener("mouseup", this.#docDrag.mouseup);
    document.addEventListener("touchmove", this.#docDrag.touchmove, { passive: false });
    document.addEventListener("touchend", this.#docDrag.touchend);
    document.addEventListener("touchcancel", this.#docDrag.touchcancel);
  }
  #startDrag(y) { this.#drag = { active: true, startY: y, distance: 0 }; this.panel.classList.add("arrastrando"); }
  #moveDrag(y) {
    if (!this.#drag.active) return;
    this.#drag.distance = Math.max(0, y - this.#drag.startY);
    this.panel.style.transform = `translateY(${this.#drag.distance}px)`;
  }
  #endDrag() {
    if (!this.#drag.active) return;
    this.panel.classList.remove("arrastrando");
    if (this.#drag.distance > 100 && !this.checkout.isBusy && this.list.ensureSaved()) this.close();
    else this.panel.style.transform = "";
    this.#drag = { active: false, startY: 0, distance: 0 };
  }
}
define("order-panel", OrderPanel);
