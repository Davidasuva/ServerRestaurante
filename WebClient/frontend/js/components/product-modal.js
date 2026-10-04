import { formatCOP } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";

/** <product-modal>: detalle del producto. open(product). Emite "product-confirm" { product, quantity }. */
class ProductModal extends BaseElement {
  #product = null;
  #onKey = (event) => { if (event.key === "Escape" && this.isOpen) this.close(); };

  connectedCallback() {
    this.innerHTML = `
      <div class="producto-overlay" aria-hidden="true">
        <section class="producto-modal" role="dialog" aria-modal="true" aria-labelledby="titulo-producto-modal">
          <button class="cerrar-producto" type="button" aria-label="Cerrar detalles del producto">
            <span class="material-symbols-outlined">close</span>
          </button>
          <img class="producto-modal-imagen" src="" alt="" />
          <div class="producto-modal-contenido">
            <h2 id="titulo-producto-modal"></h2>
            <p class="producto-modal-precio"></p>
            <p class="producto-modal-descripcion"></p>
            <button class="confirmar-producto" type="button">Añadir al pedido</button>
          </div>
        </section>
      </div>`;
    this.overlay = this.querySelector(".producto-overlay");
    this.querySelector(".cerrar-producto").addEventListener("click", () => this.close());
    this.overlay.addEventListener("click", (e) => { if (e.target === this.overlay) this.close(); });
    this.querySelector(".confirmar-producto").addEventListener("click", () => {
      this.emit("product-confirm", { product: this.#product, quantity: 1 });
      this.close();
    });
    document.addEventListener("keydown", this.#onKey);
  }
  disconnectedCallback() { document.removeEventListener("keydown", this.#onKey); }

  get isOpen() { return this.overlay?.classList.contains("abierto") ?? false; }

  open(product) {
    this.#product = product;
    const image = this.querySelector(".producto-modal-imagen");
    image.src = product.image;
    image.alt = product.name;
    this.querySelector("#titulo-producto-modal").textContent = product.name;
    this.querySelector(".producto-modal-precio").textContent = formatCOP(product.price);
    this.querySelector(".producto-modal-descripcion").textContent = product.description;
    this.#toggle(true);
  }
  close() { this.#toggle(false); }

  #toggle(open) {
    this.overlay.classList.toggle("abierto", open);
    this.overlay.setAttribute("aria-hidden", String(!open));
    document.body.classList.toggle("producto-abierto", open);
  }
}
define("product-modal", ProductModal);
