import { esc } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";

let uid = 0;

/** <confirm-dialog>: const ok = await dialog.ask({ title, message, confirmLabel, cancelLabel }) */
class ConfirmDialog extends BaseElement {
  #resolve = null;
  #closeTimer = null;

  connectedCallback() {
    const id = ++uid;
    this.innerHTML = `
      <div class="confirm-dialog-overlay" aria-hidden="true" hidden>
        <div class="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="cd-title-${id}" aria-describedby="cd-desc-${id}">
          <div class="confirm-dialog-icon"><span class="material-symbols-outlined">help_outline</span></div>
          <h3 id="cd-title-${id}"></h3>
          <p id="cd-desc-${id}"></p>
          <div class="confirm-dialog-actions">
            <button class="confirm-dialog-btn cancel" type="button"></button>
            <button class="confirm-dialog-btn confirm" type="button"></button>
          </div>
        </div>
      </div>`;
    this.overlay = this.querySelector(".confirm-dialog-overlay");
    this.overlay.addEventListener("click", (e) => { if (e.target === this.overlay) this.#settle(false); });
    this.querySelector(".cancel").addEventListener("click", () => this.#settle(false));
    this.querySelector(".confirm").addEventListener("click", () => this.#settle(true));
  }

  get isOpen() { return this.overlay?.classList.contains("abierto") ?? false; }

  ask({ title = "¿Continuar?", message = "", confirmLabel = "Aceptar", cancelLabel = "Cancelar" } = {}) {
    this.#resolve?.(false);
    clearTimeout(this.#closeTimer);
    this.querySelector("h3").textContent = title;
    this.querySelector("p").textContent = message;
    this.querySelector(".cancel").textContent = cancelLabel;
    this.querySelector(".confirm").textContent = confirmLabel;
    this.overlay.hidden = false;
    requestAnimationFrame(() => {
      this.overlay.classList.add("abierto");
      this.overlay.setAttribute("aria-hidden", "false");
      this.querySelector(".confirm").focus();
    });
    return new Promise((resolve) => { this.#resolve = resolve; });
  }

  /** Cierra sin confirmar (Escape). */
  dismiss() { this.#settle(false); }

  #settle(result) {
    this.overlay.classList.remove("abierto");
    this.overlay.setAttribute("aria-hidden", "true");
    this.#closeTimer = setTimeout(() => { if (!this.isOpen) this.overlay.hidden = true; }, 250);
    const resolve = this.#resolve;
    this.#resolve = null;
    resolve?.(result);
  }
}
define("confirm-dialog", ConfirmDialog);
