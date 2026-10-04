import { esc } from "../utils/format.js";
import { demoLoadingDelay, setLoading, waitForImages } from "../utils/loading.js";
import { BaseElement, define } from "./base-element.js";
import "./product-card.js";

const LOADING_CLASS = "productos-cargando";
const SKELETON_CARDS = 8; // tarjetas "fantasma" mientras llega GET /api/productos

/** Capa de esqueleto que cubre una tarjeta (estilos en products.css → .producto-skeleton). */
function addProductSkeleton(card) {
  if (card.querySelector(".producto-skeleton")) return;

  const skeleton = document.createElement("div");
  skeleton.className = "producto-skeleton";
  skeleton.setAttribute("aria-hidden", "true");
  skeleton.innerHTML = `
    <span class="producto-skeleton-image skeleton"></span>
    <span class="producto-skeleton-title skeleton"></span>
    <span class="producto-skeleton-price skeleton"></span>
    <span class="producto-skeleton-button skeleton"></span>`;
  card.append(skeleton);
}

/**
 * <product-grid>: catálogo. Métodos: showLoading(), showError(msg), setProducts(list), applyFilter({category, search}), markAdded(id).
 * Los eventos de las tarjetas ("product-select", "product-add") burbujean hasta aquí.
 */
class ProductGrid extends BaseElement {
  #filter = { category: "todas", search: "" };

  /** Pantalla de carga: tarjetas esqueleto hasta que lleguen los productos. */
  showLoading() {
    this.replaceChildren();
    this.#empty = null;
    setLoading(this, true, LOADING_CLASS);
    this.append(...Array.from({ length: SKELETON_CARDS }, () => {
      const card = document.createElement("div");
      card.className = "producto-card";
      addProductSkeleton(card);
      return card;
    }));
  }

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
    // El esqueleto se mantiene hasta que carguen las imágenes, para que las tarjetas no aparezcan "a saltos".
    cards.forEach(addProductSkeleton);
    setLoading(this, true, LOADING_CLASS);
    this.#empty = document.createElement("p");
    this.#empty.className = "catalogo-estado";
    this.#empty.hidden = true;
    this.#empty.textContent = "No encontramos productos con ese filtro.";
    this.append(this.#empty);
    this.applyFilter(this.#filter);
    const images = [...this.querySelectorAll("product-card:not(.oculto) img")];
    waitForImages(images, () => {
      window.setTimeout(() => setLoading(this, false, LOADING_CLASS), demoLoadingDelay());
    });
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
    setLoading(this, false, LOADING_CLASS);
    this.innerHTML = `<div class="catalogo-estado ${retry ? "error" : ""}" role="${retry ? "alert" : "status"}"><p>${esc(text)}</p>${retry ? `<button type="button" class="reintentar">Reintentar</button>` : ""}</div>`;
  }
}
define("product-grid", ProductGrid);
