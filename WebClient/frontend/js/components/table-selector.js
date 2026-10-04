import { getTables } from "../services/restaurant-api.js";
import { esc } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";

/** <table-selector target="products.html">: lista las mesas (GET /api/mesas) como enlaces ?mesa=ID. */
class TableSelector extends BaseElement {
  connectedCallback() { this.load(); }

  async load() {
    this.innerHTML = `<p class="estado-carga" role="status">Cargando mesas…</p>`;
    try {
      const tables = await getTables();
      if (!tables.length) { this.#renderMessage("No hay mesas disponibles."); return; }
      const target = this.getAttribute("target") || "products.html";
      this.style.setProperty("--filas-mesas", Math.ceil(tables.length / 2));
      this.innerHTML = tables.map((t) =>
        `<div class="btn-mesa"><a href="${esc(target)}?mesa=${t.id}">Mesa ${t.id}</a></div>`).join("");
    } catch (error) {
      this.#renderMessage(error.message, true);
    }
  }

  #renderMessage(message, retry = false) {
    this.innerHTML = `<div class="estado-carga error" role="alert"><p>${esc(message)}</p>${retry ? `<button type="button" class="reintentar">Reintentar</button>` : ""}</div>`;
    this.querySelector(".reintentar")?.addEventListener("click", () => this.load());
  }
}
define("table-selector", TableSelector);
