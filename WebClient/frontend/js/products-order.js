import { availableExtras, elements, state } from "./products-state.js";
import { setConfirmReturnOpen, showToast } from "./products-ui.js";

export function getOrderItemTotal(item) {
  const extrasTotal = item.extraIngredients.reduce((total, ingredient) => {
    const extraName = typeof ingredient === "string" ? ingredient : ingredient.name;
    const extraQuantity = typeof ingredient === "string" ? 1 : ingredient.quantity;
    const extra = availableExtras.find((availableExtra) => availableExtra.name === extraName);
    return total + (extra?.price || 0) * extraQuantity;
  }, 0);
  return (item.price + extrasTotal) * item.quantity;
}

export function updateOrderSummary() {
  elements.orderButton.classList.toggle("pedido-con-productos", state.orderItems.length > 0);
  document.querySelector(".cantidad-pedido").textContent = state.orderQuantity;
  document.querySelector(".total-pedido").textContent = `$${state.orderTotal.toLocaleString("es-CO")}`;
  elements.orderTotalElement.textContent = `$${state.orderTotal.toLocaleString("es-CO")}`;
  elements.emptyOrderState.hidden = state.orderItems.length > 0;
  if (state.orderItems.length > 0) elements.emptyOrderState.classList.remove("apareciendo");
  elements.orderList.hidden = state.orderItems.length === 0;
  elements.confirmOrderButton.disabled = state.orderItems.length === 0;
}

export function renderCheckout() {
  elements.checkoutLines.innerHTML = state.orderItems.map((item) => `
    <div class="checkout-linea">
      <img src="${item.image}" alt="">
      <span class="checkout-linea-info"><strong>${item.name}</strong><small>${item.quantity} unidad${item.quantity === 1 ? "" : "es"}</small></span>
      <strong class="checkout-linea-precio">$${getOrderItemTotal(item).toLocaleString("es-CO")}</strong>
    </div>
  `).join("");
  elements.checkoutTotal.textContent = `$${state.orderTotal.toLocaleString("es-CO")}`;
}

export function showCheckout() {
  setConfirmReturnOpen(false);
  renderCheckout();
  elements.finalizeCheckoutButton.disabled = false;
  elements.finalizeCheckoutButton.textContent = "Enviar pedido a cocina";
  elements.backToSummaryButton.disabled = false;
  const paymentFieldset = document.querySelector(".checkout-payment");
  if (paymentFieldset) paymentFieldset.disabled = false;
  document.querySelectorAll('input[name="metodo-pago"]').forEach((input) => { input.disabled = false; });
  elements.checkoutView.classList.remove("pedido-confirmado");
  elements.orderList.hidden = true;
  elements.emptyOrderState.hidden = true;
  document.querySelector(".panel-pedido-footer").hidden = true;
  elements.checkoutView.hidden = false;
  showToast("Pedido confirmado", "success");
}

export function showOrderSummary() {
  if (elements.backToSummaryButton.disabled) return;
  setConfirmReturnOpen(false);
  elements.checkoutView.hidden = true;
  document.querySelector(".panel-pedido-footer").hidden = false;
  updateOrderSummary();
}

export function getExtraQuantity(item, extraName) {
  const extra = item.extraIngredients.find((ingredient) => typeof ingredient === "string" ? ingredient === extraName : ingredient.name === extraName);
  return typeof extra === "string" ? 1 : extra?.quantity || 0;
}

export function setExtraQuantity(item, extraName, quantity) {
  const extraIndex = item.extraIngredients.findIndex((ingredient) => typeof ingredient === "string" ? ingredient === extraName : ingredient.name === extraName);
  if (quantity === 0) {
    if (extraIndex >= 0) item.extraIngredients.splice(extraIndex, 1);
    return;
  }
  if (extraIndex >= 0) item.extraIngredients[extraIndex] = { name: extraName, quantity };
  else item.extraIngredients.push({ name: extraName, quantity });
}

export function renderOrderEditor(item, index, animateEditor) {
  const ingredients = item.card.dataset.ingredientes.split(",").map((ingredient) => ingredient.trim());
  return `<div class="pedido-editor ${animateEditor ? "animar" : ""}" data-editor-index="${index}">
    <h3>Ingredientes</h3>
    <div class="ingredientes-lista">${ingredients.map((ingredient) => `<label class="ingrediente-fila"><input type="radio" data-ingrediente="${ingredient}" ${item.removedIngredients.includes(ingredient) ? "" : "checked"}><span>${ingredient}</span></label>`).join("")}</div>
    <h3>Adicionales</h3>
    <div class="adicionales-lista">${availableExtras.map((extra) => `<div class="adicional-fila ${getExtraQuantity(item, extra.name) > 0 ? "seleccionado" : ""}" data-extra="${extra.name}"><span class="adicional-info"><strong>${extra.name}</strong><small>+$${extra.price.toLocaleString("es-CO")}</small></span><span class="selector-adicional" aria-label="Cantidad de ${extra.name}"><button class="cambiar-adicional disminuir-adicional" type="button" aria-label="Disminuir ${extra.name}" ${getExtraQuantity(item, extra.name) === 0 ? "disabled" : ""}>−</button><strong class="cantidad-adicional">${getExtraQuantity(item, extra.name)}</strong><button class="cambiar-adicional aumentar-adicional" type="button" aria-label="Aumentar ${extra.name}">+</button></span></div>`).join("")}</div>
    <div class="pedido-editor-footer"><button class="guardar-edicion" type="button">Guardar cambios</button></div>
  </div>`;
}

export function renderOrderItems(animateEditor = false) {
  elements.orderList.innerHTML = state.orderItems.map((item, index) => `<div class="pedido-item ${state.expandedOrderIndex === index ? "expandido" : ""}" data-index="${index}">
    <div class="pedido-item-resumen" role="button" tabindex="0" aria-expanded="${state.expandedOrderIndex === index}"><img src="${item.image}" alt="${item.name}"><span class="pedido-item-info"><strong>${item.name}</strong><span>$${item.price.toLocaleString("es-CO")}</span></span><span class="pedido-item-cantidad">x${item.quantity}</span><button class="eliminar-pedido" type="button" aria-label="Eliminar ${item.name}"><span class="material-symbols-outlined">delete</span></button></div>
    ${state.expandedOrderIndex === index ? renderOrderEditor(item, index, animateEditor) : ""}</div>`).join("");
  elements.orderList.querySelectorAll(".pedido-item-resumen").forEach((itemButton) => {
    const toggleItem = (event) => {
      if (event.target.closest(".eliminar-pedido")) return;
      const index = Number(itemButton.closest(".pedido-item").dataset.index);
      if (state.expandedOrderIndex === index) collapseOrderItem(index);
      else openOrderItem(index);
    };
    itemButton.addEventListener("click", toggleItem);
    itemButton.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") { event.preventDefault(); toggleItem(event); }
    });
    itemButton.querySelector(".eliminar-pedido").addEventListener("click", () => removeOrderItem(Number(itemButton.closest(".pedido-item").dataset.index)));
  });
  bindOrderEditor();
}

export function bindOrderEditor() {
  const editor = elements.orderList.querySelector(".pedido-editor");
  if (!editor) return;
  const index = Number(editor.dataset.editorIndex);
  const item = state.orderItems[index];
  editor.querySelectorAll(".ingrediente-fila").forEach((row) => {
    const input = row.querySelector("input");
    row.addEventListener("click", (event) => { event.preventDefault(); input.checked = !input.checked; state.unsavedOrderIndex = index; });
  });
  editor.querySelectorAll(".adicional-fila").forEach((row) => {
    const extra = row.dataset.extra;
    row.querySelector(".disminuir-adicional").addEventListener("click", () => { setExtraQuantity(item, extra, Math.max(0, getExtraQuantity(item, extra) - 1)); state.unsavedOrderIndex = index; calculateOrderSummary(); });
    row.querySelector(".aumentar-adicional").addEventListener("click", () => { setExtraQuantity(item, extra, getExtraQuantity(item, extra) + 1); state.unsavedOrderIndex = index; calculateOrderSummary(); });
  });
  editor.querySelector(".guardar-edicion").addEventListener("click", () => {
    item.removedIngredients = [...editor.querySelectorAll(".ingrediente-fila input:not(:checked)")].map((input) => input.dataset.ingrediente);
    editor.querySelector(".guardar-edicion").disabled = true;
    state.unsavedOrderIndex = null;
    collapseOrderItem(index);
  });
}

export function ensureNoUnsavedChanges(index = null) {
  if (state.unsavedOrderIndex === null || (index !== null && state.unsavedOrderIndex !== index)) return true;
  showToast("Guarda los cambios del pedido abierto antes de continuar.", "error");
  return false;
}

export function collapseOrderItem(index) {
  if (!ensureNoUnsavedChanges(index)) return;
  const editor = elements.orderList.querySelector(`[data-editor-index="${index}"]`);
  if (!editor) return;
  editor.classList.add("cerrando");
  setTimeout(() => { state.expandedOrderIndex = null; calculateOrderSummary(); }, 380);
}

export function openOrderItem(index) {
  if (!ensureNoUnsavedChanges()) return;
  const currentEditor = elements.orderList.querySelector(".pedido-editor");
  if (state.expandedOrderIndex === null || !currentEditor) { state.expandedOrderIndex = index; renderOrderItems(true); return; }
  currentEditor.classList.add("cerrando");
  setTimeout(() => { state.expandedOrderIndex = index; renderOrderItems(true); }, 380);
}

export function removeOrderItem(index) {
  if (!ensureNoUnsavedChanges(index)) return;
  const orderItem = elements.orderList.querySelector(`[data-index="${index}"]`);
  if (!orderItem) return;
  orderItem.classList.add("eliminando");
  setTimeout(() => { state.orderItems.splice(index, 1); state.expandedOrderIndex = null; state.unsavedOrderIndex = null; calculateOrderSummary(); if (state.orderItems.length === 0) animateEmptyOrderState(); }, 320);
}

export function animateEmptyOrderState() {
  elements.emptyOrderState.classList.remove("preparando-entrada");
  elements.emptyOrderState.classList.add("preparando-entrada");
  requestAnimationFrame(() => requestAnimationFrame(() => elements.emptyOrderState.classList.remove("preparando-entrada")));
}

export function calculateOrderSummary() {
  state.orderQuantity = state.orderItems.reduce((total, item) => total + item.quantity, 0);
  state.orderTotal = state.orderItems.reduce((total, item) => total + getOrderItemTotal(item), 0);
  renderOrderItems();
  updateOrderSummary();
}
