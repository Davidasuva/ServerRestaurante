import { elements, productResetTimers, state, tableNumber } from "./products-state.js";
import { parseProductPrice, setConfirmReturnOpen, setOrderPanelOpen, setProductModalOpen, showToast } from "./products-ui.js";
import { calculateOrderSummary, ensureNoUnsavedChanges, showCheckout, showOrderSummary } from "./products-order.js";
import { createOrderPayload, submitOrder } from "./restaurant-api.js";

function openProductModal(card, orderItem = null) {
  state.selectedProductCard = card;
  state.selectedOrderItem = orderItem;
  state.productQuantity = orderItem ? orderItem.quantity : 1;
  elements.modalImage.src = card.querySelector("img").src;
  elements.modalImage.alt = card.querySelector("img").alt;
  elements.modalTitle.textContent = card.querySelector("h2").textContent;
  elements.modalPrice.textContent = card.querySelector(".producto-precio").textContent;
  elements.modalDescription.textContent = card.dataset.descripcion;
  elements.confirmProductButton.textContent = "Añadir al pedido";
  setProductModalOpen(true);
}

function updateProducts() {
  const selectedCategory = document.querySelector(".filtro-opcion.activo").dataset.categoria;
  const searchText = elements.searchInput.value.trim().toLowerCase();
  elements.productCards.forEach((card) => {
    const matchesCategory = selectedCategory === "todas" || card.dataset.categoria === selectedCategory;
    const matchesSearch = card.textContent.toLowerCase().includes(searchText);
    card.classList.toggle("oculto", !matchesCategory || !matchesSearch);
  });
}

function markProductAsAdded(button) {
  const resetTimer = productResetTimers.get(button);
  clearTimeout(resetTimer);
  button.classList.add("producto-anadido");
  button.querySelector(".material-symbols-outlined").textContent = "check_circle";
  button.lastChild.textContent = " Añadido";
  button.setAttribute("aria-pressed", "true");
  productResetTimers.set(button, setTimeout(() => {
    button.classList.remove("producto-anadido");
    button.querySelector(".material-symbols-outlined").textContent = "add";
    button.lastChild.textContent = " Añadir";
    button.removeAttribute("aria-pressed");
  }, 1000));
}

function bindOrderDrag() {
  const finishDrag = () => {
    if (!state.isDraggingOrderPanel) return;
    state.isDraggingOrderPanel = false;
    elements.orderPanel.classList.remove("arrastrando");
    if (state.dragDistance > 100 && ensureNoUnsavedChanges()) setOrderPanelOpen(false);
    else elements.orderPanel.style.transform = "";
    state.dragDistance = 0;
  };

  const startDrag = (clientY) => {
    state.isDraggingOrderPanel = true;
    state.dragStartY = clientY;
    state.dragDistance = 0;
    elements.orderPanel.classList.add("arrastrando");
  };

  const moveDrag = (clientY) => {
    if (!state.isDraggingOrderPanel) return;
    state.dragDistance = Math.max(0, clientY - state.dragStartY);
    elements.orderPanel.style.transform = `translateY(${state.dragDistance}px)`;
  };

  elements.orderDragHandle.addEventListener("mousedown", (event) => {
    event.preventDefault();
    startDrag(event.clientY);
  });
  document.addEventListener("mousemove", (event) => moveDrag(event.clientY));
  document.addEventListener("mouseup", finishDrag);
  elements.orderDragHandle.addEventListener("touchstart", (event) => {
    event.preventDefault();
    startDrag(event.touches[0].clientY);
  }, { passive: false });
  document.addEventListener("touchmove", (event) => {
    if (state.isDraggingOrderPanel) moveDrag(event.touches[0].clientY);
  }, { passive: false });
  document.addEventListener("touchend", finishDrag);
  document.addEventListener("touchcancel", finishDrag);
}

function bindProductEvents() {
  elements.filterOptions.forEach((option) => option.addEventListener("click", () => {
    elements.filterOptions.forEach((item) => item.classList.remove("activo"));
    option.classList.add("activo");
    updateProducts();
  }));
  elements.searchInput.addEventListener("input", updateProducts);
  elements.productCards.forEach((card) => card.addEventListener("click", (event) => {
    if (!event.target.closest(".agregar-producto")) openProductModal(card);
  }));
  elements.addProductButtons.forEach((button) => button.addEventListener("click", () => {
    if (elements.finalizeCheckoutButton.disabled) { showToast("El pedido ya fue enviado a cocina", "error"); return; }
    const card = button.closest(".producto-card");
    const existingItem = state.orderItems.find((item) => item.card === card);
    if (existingItem) existingItem.quantity += 1;
    else state.orderItems.push({ card, name: card.querySelector("h2").textContent, image: card.querySelector("img").src, price: parseProductPrice(card), quantity: 1, removedIngredients: [], extraIngredients: [] });
    calculateOrderSummary();
    markProductAsAdded(button);
  }));
}

function bindModalEvents() {
  elements.closeProductButton.addEventListener("click", () => setProductModalOpen(false));
  elements.productOverlay.addEventListener("click", (event) => { if (event.target === elements.productOverlay) setProductModalOpen(false); });
  elements.confirmProductButton.addEventListener("click", () => {
    if (elements.finalizeCheckoutButton.disabled) { showToast("El pedido ya fue enviado a cocina", "error"); setProductModalOpen(false); return; }
    const card = state.selectedProductCard;
    const itemData = { card, name: card.querySelector("h2").textContent, image: card.querySelector("img").src, price: parseProductPrice(card), quantity: state.productQuantity, removedIngredients: [], extraIngredients: [] };
    if (state.selectedOrderItem) Object.assign(state.selectedOrderItem, itemData);
    else state.orderItems.push(itemData);
    markProductAsAdded(card.querySelector(".agregar-producto"));
    calculateOrderSummary();
    setProductModalOpen(false);
  });
}

function bindOrderEvents() {
  elements.orderButton.addEventListener("click", () => setOrderPanelOpen(true));
  elements.backToMenuButton.addEventListener("click", () => { if (ensureNoUnsavedChanges()) setOrderPanelOpen(false); });
  elements.confirmOrderButton.addEventListener("click", () => { if (!elements.confirmOrderButton.disabled && ensureNoUnsavedChanges()) showCheckout(); });
  elements.backToSummaryButton.addEventListener("click", () => { if (!elements.backToSummaryButton.disabled) setConfirmReturnOpen(true); });
  elements.cancelReturnButton?.addEventListener("click", () => setConfirmReturnOpen(false));
  elements.acceptReturnButton?.addEventListener("click", () => { setConfirmReturnOpen(false); showOrderSummary(); });
  elements.confirmReturnOverlay?.addEventListener("click", (event) => { if (event.target === elements.confirmReturnOverlay) setConfirmReturnOpen(false); });
  elements.orderOverlay.addEventListener("click", (event) => { if (event.target === elements.orderOverlay) setOrderPanelOpen(false); });
}

async function sendOrderToKitchen() {
  const paymentMethod = document.querySelector('input[name="metodo-pago"]:checked').value;
  const currentMesa = tableNumber || "1";
  const payload = createOrderPayload(state.orderItems, currentMesa, paymentMethod);

  document.dispatchEvent(new CustomEvent("restaurant-order-submit", {
    detail: payload,
  }));

  elements.finalizeCheckoutButton.disabled = true;
  elements.finalizeCheckoutButton.textContent = "Pedido enviado";
  elements.backToSummaryButton.disabled = true;
  const paymentFieldset = document.querySelector(".checkout-payment");
  if (paymentFieldset) paymentFieldset.disabled = true;
  document.querySelectorAll('input[name="metodo-pago"]').forEach((input) => { input.disabled = true; });
  elements.checkoutView.classList.add("pedido-confirmado");
  try {
    const order = await submitOrder(payload);
    const orderId = order.id;
    sessionStorage.setItem("currentOrderId", orderId);
    sessionStorage.setItem("currentOrderMesa", currentMesa);
    showToast(`Pedido enviado a cocina · ${paymentMethod}`, "success");
    setTimeout(() => { window.location.href = `status.html?mesa=${encodeURIComponent(currentMesa)}&id=${encodeURIComponent(orderId)}`; }, 1600);
  } catch (error) {
    elements.finalizeCheckoutButton.disabled = false;
    elements.finalizeCheckoutButton.textContent = "Enviar pedido a cocina";
    elements.backToSummaryButton.disabled = false;
    elements.checkoutView.classList.remove("pedido-confirmado");
    showToast(error.message, "error");
  }
}

elements.finalizeCheckoutButton.addEventListener("click", sendOrderToKitchen);
bindProductEvents();
bindModalEvents();
bindOrderEvents();
bindOrderDrag();


document.addEventListener("keydown", (event) => {
  if (event.key !== "Escape") return;
  if (elements.confirmReturnOverlay && !elements.confirmReturnOverlay.hidden && elements.confirmReturnOverlay.classList.contains("abierto")) {
    setConfirmReturnOpen(false);
    return;
  }
  setOrderPanelOpen(false);
  setProductModalOpen(false);
});
