import { getTables } from "../services/restaurant-api.js";
import { esc, wait } from "../utils/format.js";
import { demoLoadingDelay, setLoading } from "../utils/loading.js";
import { BaseElement, define } from "./base-element.js";

const SKELETON_COUNT = 10; // botones "fantasma" mientras llega GET /api/mesas

/** <table-selector target="products.html">: lista las mesas (GET /api/mesas) como enlaces ?mesa=ID. */
class TableSelector extends BaseElement {
  connectedCallback() { this.load(); }

  async load() {
    this.#renderSkeleton();
    try {
      const tables = await getTables();
      const demoDelay = demoLoadingDelay();
      if (demoDelay) await wait(demoDelay);
      setLoading(this, false);
      if (!tables.length) { this.#renderMessage("No hay mesas disponibles."); return; }
      const target = this.getAttribute("target") || "products.html";
      this.style.setProperty("--filas-mesas", Math.ceil(tables.length / 2));
      this.innerHTML = tables.map((t) =>
        `<div class="btn-mesa"><a href="${esc(target)}?mesa=${t.id}">Mesa ${t.id}</a></div>`).join("");
    } catch (error) {
      setLoading(this, false);
      this.#renderMessage(error.message, true);
    }
  }

  /** Esqueleto de carga: botones de mesa vacíos con el brillo animado (estilos en tables.css → .inicio.cargando). */
  #renderSkeleton() {
    setLoading(this, true);
    this.innerHTML = Array.from({ length: SKELETON_COUNT }, () =>
      `<div class="btn-mesa" aria-hidden="true"><a tabindex="-1"></a></div>`).join("");
  }

  #renderMessage(message, retry = false) {
    this.innerHTML = `<div class="estado-carga error" role="alert"><p>${esc(message)}</p>${retry ? `<button type="button" class="reintentar">Reintentar</button>` : ""}</div>`;
    this.querySelector(".reintentar")?.addEventListener("click", () => this.load());
  }
}
define("table-selector", TableSelector);
