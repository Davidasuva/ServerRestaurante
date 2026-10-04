import { BaseElement, define } from "./base-element.js";

/** <app-toast>: mensaje temporal. Usar showToast(mensaje, "success" | "error"). */
class AppToast extends BaseElement {
  #timer = null;
  connectedCallback() {
    this.classList.add("pedido-toast");
    this.setAttribute("role", "status");
    this.setAttribute("aria-live", "polite");
  }
  show(message, type = "success", duration = 3000) {
    this.textContent = message;
    this.classList.remove("success", "error");
    this.classList.add(type, "visible");
    clearTimeout(this.#timer);
    this.#timer = setTimeout(() => this.classList.remove("visible"), duration);
  }
}
define("app-toast", AppToast);

export function showToast(message, type = "success") {
  let toast = document.querySelector("app-toast");
  if (!toast) { toast = document.createElement("app-toast"); document.body.appendChild(toast); }
  toast.show(message, type);
}
