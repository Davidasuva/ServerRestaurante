import { cartStore } from "../store/cart-store.js";
import { formatCOP } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";

/** <order-bar>: barra inferior "Ver pedido". Pide abrir el panel con el evento de documento "order-panel-open". */
class OrderBar extends BaseElement {
  #update = () => {
    this.button.classList.toggle("pedido-con-productos", cartStore.items.length > 0);
    this.querySelector(".cantidad-pedido").textContent = cartStore.quantity;
    this.querySelector(".total-pedido").textContent = formatCOP(cartStore.total);
  };
  #toggled = (event) => this.button.setAttribute("aria-expanded", String(event.detail.open));

  connectedCallback() {
    this.classList.add("cont-ver-pedido");
    this.innerHTML = `
      <button class="pedido" type="button" aria-controls="panel-pedido" aria-expanded="false">
        <span class="material-symbols-outlined">shopping_bag</span>
        <strong>Ver pedido: <span class="cantidad-pedido">0</span></strong>
        <strong class="total-pedido">$0</strong>
      </button>`;
    this.button = this.querySelector("button");
    this.button.addEventListener("click", () => document.dispatchEvent(new CustomEvent("order-panel-open")));
    cartStore.addEventListener("change", this.#update);
    document.addEventListener("order-panel-toggle", this.#toggled);
    this.#update();
  }
  disconnectedCallback() {
    cartStore.removeEventListener("change", this.#update);
    document.removeEventListener("order-panel-toggle", this.#toggled);
  }
}
define("order-bar", OrderBar);
