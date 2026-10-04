import { esc } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";

/**
 * <menu-header table="3">: logo, mesa actual, buscador y filtros por categoría.
 * Asignar `categories = [{id, label}]`. Emite "menu-filter-change" { category, search }.
 */
class MenuHeader extends BaseElement {
  static observedAttributes = ["table"];
  #categories = [];
  #category = "todas";

  connectedCallback() {
    this.innerHTML = `
      <div class="head-productos">
        <img class="restaurant-logo" src="${esc(this.getAttribute("logo") || "../assets/logo.png")}" alt="Carbón y Sazón" />
        <p>Carbón y Sazón</p>
        <label class="accent mesa-actual"></label>
      </div>
      <div class="buscador-productos">
        <input type="search" placeholder="Buscar en el menú..." aria-label="Buscar en el menú" />
        <button class="limpiar-busqueda" type="button" aria-label="Borrar búsqueda" hidden>
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      <div class="filtro" role="group" aria-label="Categorías"></div>`;
    this.input = this.querySelector("input");
    this.clearButton = this.querySelector(".limpiar-busqueda");
    this.input.addEventListener("input", () => this.#changed());
    this.clearButton.addEventListener("click", () => { this.input.value = ""; this.#changed(); this.input.focus(); });
    this.querySelector(".filtro").addEventListener("click", (event) => {
      const button = event.target.closest(".filtro-opcion");
      if (!button) return;
      this.#category = button.dataset.categoria;
      this.#renderFilters();
      this.#changed();
    });
    this.#renderTable();
    this.#renderFilters();
  }

  attributeChangedCallback() { if (this.isConnected) this.#renderTable(); }

  set categories(list) {
    this.#categories = list;
    if (!list.some((c) => c.id === this.#category)) this.#category = "todas";
    if (this.isConnected) this.#renderFilters();
  }

  #renderTable() {
    const table = this.getAttribute("table");
    this.querySelector(".mesa-actual").textContent = table ? `M. ${table}` : "";
  }

  #renderFilters() {
    const all = [{ id: "todas", label: "Todos" }, ...this.#categories];
    this.querySelector(".filtro").innerHTML = all.map((c) =>
      `<button class="filtro-opcion ${c.id === this.#category ? "activo" : ""}" type="button" data-categoria="${esc(c.id)}">${esc(c.label)}</button>`).join("");
  }

  #changed() {
    const search = this.input.value.trim();
    this.clearButton.hidden = search.length === 0;
    this.emit("menu-filter-change", { category: this.#category, search });
  }
}
define("menu-header", MenuHeader);
