import { getConfig } from "../config.js";
import { esc, formatCOP } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";

/**
 * <product-card>: tarjeta del catálogo. Asignar `product`.
 * Emite "product-select" (abrir detalle) y "product-add" (cancelable: si se cancela no muestra "Añadido").
 */
class ProductCard extends BaseElement {
  #product = null;
  #resetTimer = null;

  set product(value) {
    this.#product = value;
    this.dataset.categoria = value.category.id;
    if (this.isConnected) this.#render();
  }
  get product() { return this.#product; }

  connectedCallback() {
    this.classList.add("producto-card");
    this.addEventListener("click", (event) => {
      if (event.target.closest(".agregar-producto")) {
        if (this.emit("product-add", { product: this.#product }, { cancelable: true })) this.markAdded();
      } else {
        this.emit("product-select", { product: this.#product });
      }
    });
    if (this.#product) this.#render();
  }

  get searchText() {
    const p = this.#product;
    return `${p.name} ${p.description} ${p.category.label}`.toLowerCase();
  }

  markAdded() {
    const button = this.querySelector(".agregar-producto");
    if (!button) return;
    clearTimeout(this.#resetTimer);
    this.#setButton(button, true);
    this.#resetTimer = setTimeout(() => this.#setButton(button, false), 1000);
  }

  #setButton(button, added) {
    button.classList.toggle("producto-anadido", added);
    button.querySelector(".material-symbols-outlined").textContent = added ? "check_circle" : "add";
    button.querySelector(".etiqueta").textContent = added ? " Añadido" : " Añadir";
    if (added) button.setAttribute("aria-pressed", "true"); else button.removeAttribute("aria-pressed");
  }

  #render() {
    const p = this.#product;
    this.innerHTML = `
      <img src="${esc(p.image)}" alt="${esc(p.name)}" loading="lazy" decoding="async" />
      <div class="producto-info">
        <h2>${esc(p.name)}</h2>
        <p class="producto-precio">${formatCOP(p.price)}</p>
        <button class="agregar-producto" type="button">
          <span class="material-symbols-outlined">add</span><span class="etiqueta"> Añadir</span>
        </button>
      </div>`;
    this.querySelector("img").addEventListener("error", (e) => {
      e.target.src = getConfig().fallbackImage; // evita bucles: solo se reintenta una vez
    }, { once: true });
  }
}
define("product-card", ProductCard);
