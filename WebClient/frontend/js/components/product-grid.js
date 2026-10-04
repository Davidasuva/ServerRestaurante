import { esc } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";
import "./product-card.js";

/**
 * <product-grid>: catálogo. Métodos: showLoading(), showError(msg), setProducts(list), applyFilter({category, search}), markAdded(id).
 * Los eventos de las tarjetas ("product-select", "product-add") burbujean hasta aquí.
 */
class ProductGrid extends BaseElement {
  #filter = { category: "todas", search: "" };

  showLoading() { this.#message("Cargando menú…"); }

  showError(message) {
    this.#message(message, true);
    this.querySelector(".reintentar").addEventListener("click", () => this.emit("catalog-retry"));
  }

  setProducts(products) {
    this.replaceChildren();
    if (!products.length) { this.#message("Aún no hay productos en el menú."); return; }
    const cards = products.map((product) => {
      const card = document.createElement("product-card");
      card.product = product;
      return card;
    });
    this.append(...cards);
    this.#empty = document.createElement("p");
    this.#empty.className = "catalogo-estado";
    this.#empty.hidden = true;
    this.#empty.textContent = "No encontramos productos con ese filtro.";
    this.append(this.#empty);
    this.applyFilter(this.#filter);
  }

  #empty = null;

  applyFilter(filter) {
    this.#filter = filter;
    const search = filter.search.toLowerCase();
    let visible = 0;
    this.querySelectorAll("product-card").forEach((card) => {
      const show = (filter.category === "todas" || card.dataset.categoria === filter.category)
        && card.searchText.includes(search);
      card.classList.toggle("oculto", !show);
      if (show) visible += 1;
    });
    if (this.#empty) this.#empty.hidden = visible > 0;
  }

  markAdded(productId) {
    [...this.querySelectorAll("product-card")].find((c) => c.product.id === productId)?.markAdded();
  }

  #message(text, retry = false) {
    this.innerHTML = `<div class="catalogo-estado ${retry ? "error" : ""}" role="${retry ? "alert" : "status"}"><p>${esc(text)}</p>${retry ? `<button type="button" class="reintentar">Reintentar</button>` : ""}</div>`;
  }
}
define("product-grid", ProductGrid);
