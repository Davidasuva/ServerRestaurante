export class BaseElement extends HTMLElement {
  /** Dispara un CustomEvent que burbujea. Devuelve false si algún listener llamó preventDefault(). */
  emit(name, detail, { cancelable = false } = {}) {
    return this.dispatchEvent(new CustomEvent(name, { bubbles: true, composed: true, cancelable, detail }));
  }
}

export function define(tag, constructor) {
  if (!customElements.get(tag)) customElements.define(tag, constructor);
}
