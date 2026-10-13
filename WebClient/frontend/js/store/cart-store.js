import { getConfig } from "../config.js";

/**
 * Estado del pedido en curso. Emite "change" cada vez que algo cambia.
 * Los componentes se suscriben a este store en lugar de compartir variables globales.
 */
class CartStore extends EventTarget {
  items = [];
  extras = [];
  locked = false; // true una vez enviado el pedido a cocina

  setExtras(list) { this.extras = list; this.#changed(); }

  itemTotal(item) {
    const extras = item.extraIngredients.reduce((sum, e) => sum + e.price * e.quantity, 0);
    return (item.price + extras) * item.quantity;
  }
  get quantity() { return this.items.reduce((sum, i) => sum + i.quantity, 0); }
  get total() { return this.items.reduce((sum, i) => sum + this.itemTotal(i), 0); }
  isDrink(item) { return getConfig().drinkCategories.includes(item.product.category.id); }

  add(product, quantity = 1) {
    if (this.locked) return false;
    const existing = this.items.find((i) => i.product.id === product.id);
    if (existing) existing.quantity += quantity;
    else this.items.push({
      product, name: product.name, image: product.image, price: product.price, quantity,
      drinkOption: product.drinkOptions[0] ?? null, removedIngredients: [], extraIngredients: [],
    });
    this.#changed();
    return true;
  }
  remove(index) { this.items.splice(index, 1); this.#changed(); }
  // Estos dos no emiten "change" a propósito: no afectan totales y evitan re-renderizar el editor abierto.
  setDrinkOption(index, option) { this.items[index].drinkOption = option; }
  setRemovedIngredients(index, names) { this.items[index].removedIngredients = names; }

  extraQuantity(item, name) { return item.extraIngredients.find((e) => e.name === name)?.quantity ?? 0; }
  setExtraQuantity(index, extra, quantity) {
    const item = this.items[index];
    const at = item.extraIngredients.findIndex((e) => e.name === extra.name);
    if (quantity <= 0) { if (at >= 0) item.extraIngredients.splice(at, 1); }
    else if (at >= 0) item.extraIngredients[at].quantity = quantity;
    else item.extraIngredients.push({ id: extra.id, name: extra.name, price: extra.price, quantity });
    this.#changed();
  }

  lock() { this.locked = true; this.#changed(); }
  clear() { this.items = []; this.locked = false; this.#changed(); }
  #changed() { this.dispatchEvent(new CustomEvent("change")); }
}

export const cartStore = new CartStore();
