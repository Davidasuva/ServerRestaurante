import { cartStore } from "../store/cart-store.js";
import { esc, formatCOP } from "../utils/format.js";
import { BaseElement, define } from "./base-element.js";
import { showToast } from "./app-toast.js";

const ANIMATION_MS = 380;

/**
 * <order-list>: líneas del pedido con editor de ingredientes/adicionales y opciones de bebida.
 * Lee y modifica el cartStore. ensureSaved() avisa si hay cambios sin guardar.
 */
class OrderList extends BaseElement {
  #expanded = null;
  #unsaved = null;
  #draftRemoved = new Set();
  #animateEditor = false;
  #render = () => this.render();

  connectedCallback() {
    this.classList.add("lista-pedido");
    this.setAttribute("aria-live", "polite");
    this.addEventListener("click", (e) => this.#onClick(e));
    this.addEventListener("keydown", (e) => this.#onKeydown(e));
    this.addEventListener("change", (e) => this.#onChange(e));
    cartStore.addEventListener("change", this.#render);
    this.render();
  }
  disconnectedCallback() { cartStore.removeEventListener("change", this.#render); }

  /** false (y aviso) si hay un editor con cambios sin guardar. */
  ensureSaved(index = null) {
    if (this.#unsaved === null || (index !== null && this.#unsaved !== index)) return true;
    showToast("Guarda los cambios del pedido abierto antes de continuar.", "error");
    return false;
  }

  render() {
    const animate = this.#animateEditor;
    this.#animateEditor = false;
    this.innerHTML = cartStore.items.map((item, index) => this.#itemTemplate(item, index, animate)).join("");
  }

  /* ---------- plantillas ---------- */

  #itemTemplate(item, index, animate) {
    const isDrink = cartStore.isDrink(item);
    const open = this.#expanded === index;
    let body = "";
    if (open) body = isDrink ? this.#drinkTemplate(item, index) : this.#editorTemplate(item, index, animate);
    return `<div class="pedido-item ${open ? "expandido" : ""} ${isDrink ? "sin-edicion" : ""}" data-index="${index}">
      <div class="pedido-item-resumen" role="button" tabindex="0" aria-expanded="${open}">
        <img src="${esc(item.image)}" alt="${esc(item.name)}">
        <span class="pedido-item-info"><strong>${esc(item.name)}</strong><span>${formatCOP(item.price)}</span></span>
        <span class="pedido-item-cantidad">x${item.quantity}</span>
        <button class="eliminar-pedido" type="button" aria-label="Eliminar ${esc(item.name)}"><span class="material-symbols-outlined">delete</span></button>
      </div>${body}</div>`;
  }

  #drinkTemplate(item, index) {
    const options = item.product.drinkOptions;
    const radios = options.length < 2 ? "" : `<fieldset class="bebida-opciones"><legend>Selecciona una opción</legend>
      ${options.map((o) => `<label class="ingrediente-fila bebida-opcion"><input type="radio" name="bebida-${index}" value="${esc(o)}" ${item.drinkOption === o ? "checked" : ""}><span>${esc(o)}</span></label>`).join("")}
    </fieldset>`;
    return `<div class="pedido-item-descripcion animar"><p>${esc(item.product.description)}</p>${radios}</div>`;
  }

  #editorTemplate(item, index, animate) {
    const ingredients = item.product.ingredients;
    return `<div class="pedido-editor ${animate ? "animar" : ""}" data-editor-index="${index}">
      <h3>Ingredientes</h3>
      <div class="ingredientes-lista">${ingredients.map((ing) => `
        <label class="ingrediente-fila ${ing.base ? "ingrediente-base" : ""}"><input type="radio" data-ingrediente="${esc(ing.name)}" ${ing.base || !this.#draftRemoved.has(ing.name) ? "checked" : ""} ${ing.base ? "disabled" : ""}><span>${esc(ing.name)}</span>${ing.base ? "<small>Base</small>" : ""}</label>`).join("")}
      </div>
      <h3>Adicionales</h3>
      <div class="adicionales-lista">${cartStore.extras.map((extra) => {
        const qty = cartStore.extraQuantity(item, extra.name);
        return `<div class="adicional-fila ${qty > 0 ? "seleccionado" : ""}" data-extra="${esc(extra.name)}">
          <span class="adicional-info"><strong>${esc(extra.name)}</strong><small>+${formatCOP(extra.price)}</small></span>
          <span class="selector-adicional" aria-label="Cantidad de ${esc(extra.name)}">
            <button class="cambiar-adicional disminuir-adicional" type="button" aria-label="Disminuir ${esc(extra.name)}" ${qty === 0 ? "disabled" : ""}>−</button>
            <strong class="cantidad-adicional">${qty}</strong>
            <button class="cambiar-adicional aumentar-adicional" type="button" aria-label="Aumentar ${esc(extra.name)}">+</button>
          </span></div>`;
      }).join("")}</div>
      <div class="pedido-editor-footer"><button class="guardar-edicion" type="button">Guardar cambios</button></div>
    </div>`;
  }

  /* ---------- eventos (delegados) ---------- */

  #indexOf(target) { return Number(target.closest(".pedido-item")?.dataset.index); }

  #onClick(event) {
    const { target } = event;
    const index = this.#indexOf(target);
    if (target.closest(".eliminar-pedido")) return this.#removeItem(index);
    if (target.closest(".guardar-edicion")) return this.#save(index);

    const adjust = target.closest(".cambiar-adicional");
    if (adjust) {
      const name = adjust.closest(".adicional-fila").dataset.extra;
      const extra = cartStore.extras.find((e) => e.name === name);
      const current = cartStore.extraQuantity(cartStore.items[index], name);
      this.#unsaved = index;
      cartStore.setExtraQuantity(index, extra, current + (adjust.classList.contains("aumentar-adicional") ? 1 : -1));
      return;
    }

    const row = target.closest(".pedido-editor .ingrediente-fila");
    if (row) {
      const input = row.querySelector("input");
      if (input.disabled) return;
      event.preventDefault();
      input.checked = !input.checked;
      if (input.checked) this.#draftRemoved.delete(input.dataset.ingrediente);
      else this.#draftRemoved.add(input.dataset.ingrediente);
      this.#unsaved = index;
      return;
    }

    if (target.closest(".pedido-item-resumen")) this.#toggle(index);
  }

  #onKeydown(event) {
    if (!event.target.matches(".pedido-item-resumen")) return;
    if (event.key === "Enter" || event.key === " ") { event.preventDefault(); this.#toggle(this.#indexOf(event.target)); }
  }

  #onChange(event) {
    if (!event.target.matches(".bebida-opcion input")) return;
    cartStore.setDrinkOption(this.#indexOf(event.target), event.target.value);
  }

  /* ---------- acciones con animación ---------- */

  #toggle(index) {
    if (this.#expanded === index) this.#collapse(index);
    else this.#open(index);
  }

  #open(index) {
    if (!this.ensureSaved()) return;
    const show = () => {
      this.#expanded = index;
      this.#draftRemoved = new Set(cartStore.items[index].removedIngredients);
      this.#animateEditor = true;
      this.render();
    };
    const current = this.querySelector(".pedido-editor, .pedido-item-descripcion");
    if (this.#expanded === null || !current) return show();
    current.classList.add("cerrando");
    setTimeout(show, ANIMATION_MS);
  }

  #collapse(index, beforeRender = null) {
    if (!this.ensureSaved(index)) return;
    const panel = this.querySelector(".pedido-editor, .pedido-item-descripcion");
    const finish = () => { beforeRender?.(); this.#expanded = null; this.render(); };
    if (!panel) return finish();
    panel.classList.add("cerrando");
    setTimeout(finish, ANIMATION_MS);
  }

  #save(index) {
    const editor = this.querySelector(`[data-editor-index="${index}"]`);
    const removed = [...editor.querySelectorAll(".ingrediente-fila input:not(:checked):not(:disabled)")].map((i) => i.dataset.ingrediente);
    editor.querySelector(".guardar-edicion").disabled = true;
    this.#unsaved = null;
    this.#collapse(index, () => cartStore.setRemovedIngredients(index, removed));
  }

  #removeItem(index) {
    if (!this.ensureSaved(index)) return;
    const row = this.querySelector(`[data-index="${index}"]`);
    row?.classList.add("eliminando");
    setTimeout(() => {
      this.#expanded = null;
      this.#unsaved = null;
      cartStore.remove(index);
    }, 320);
  }
}
define("order-list", OrderList);
