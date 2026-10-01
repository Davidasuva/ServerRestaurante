import { elements, state } from "./products-state.js";

export function setOrderPanelOpen(isOpen) {
  if (isOpen) elements.orderPanel.style.transform = "";
  elements.orderOverlay.classList.toggle("abierto", isOpen);
  elements.orderOverlay.setAttribute("aria-hidden", String(!isOpen));
  elements.orderButton.setAttribute("aria-expanded", String(isOpen));
  document.body.classList.toggle("pedido-abierto", isOpen);
}

export function setProductModalOpen(isOpen) {
  elements.productOverlay.classList.toggle("abierto", isOpen);
  elements.productOverlay.setAttribute("aria-hidden", String(!isOpen));
  document.body.classList.toggle("producto-abierto", isOpen);
}

export function setConfirmReturnOpen(isOpen) {
  if (!elements.confirmReturnOverlay) return;
  if (isOpen) {
    elements.confirmReturnOverlay.hidden = false;
    requestAnimationFrame(() => {
      elements.confirmReturnOverlay.classList.add("abierto");
      elements.confirmReturnOverlay.setAttribute("aria-hidden", "false");
    });
    return;
  }
  elements.confirmReturnOverlay.classList.remove("abierto");
  elements.confirmReturnOverlay.setAttribute("aria-hidden", "true");
  setTimeout(() => {
    if (!elements.confirmReturnOverlay.classList.contains("abierto")) {
      elements.confirmReturnOverlay.hidden = true;
    }
  }, 250);
}

export function parseProductPrice(card) {
  return Number(
    card.querySelector(".producto-precio").textContent.replace(/[^0-9]/g, ""),
  );
}

export function showToast(message, type = "success") {
  let toast = document.querySelector(".pedido-toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.className = "pedido-toast";
    document.body.appendChild(toast);
  }
  toast.textContent = message;
  toast.classList.remove("success", "error");
  toast.classList.add(type, "visible");
  clearTimeout(state.toastTimer);
  state.toastTimer = setTimeout(() => toast.classList.remove("visible"), 3000);
}
