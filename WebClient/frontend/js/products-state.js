export const elements = {
  filterOptions: document.querySelectorAll(".filtro-opcion"),
  productCards: document.querySelectorAll(".producto-card"),
  searchInput: document.querySelector(".buscador-productos input"),
  clearSearchButton: document.querySelector(".limpiar-busqueda"),
  addProductButtons: document.querySelectorAll(".agregar-producto"),
  orderButton: document.querySelector(".pedido"),
  orderOverlay: document.querySelector(".pedido-overlay"),
  orderPanel: document.querySelector(".panel-pedido"),
  orderDragHandle: document.querySelector(".arrastre-pedido"),
  backToMenuButton: document.querySelector(".volver-menu"),
  productOverlay: document.querySelector(".producto-overlay"),
  closeProductButton: document.querySelector(".cerrar-producto"),
  confirmProductButton: document.querySelector(".confirmar-producto"),
  modalTitle: document.querySelector("#titulo-producto-modal"),
  modalImage: document.querySelector(".producto-modal-imagen"),
  modalPrice: document.querySelector(".producto-modal-precio"),
  modalDescription: document.querySelector(".producto-modal-descripcion"),
  orderList: document.querySelector(".lista-pedido"),
  emptyOrderState: document.querySelector(".pedido-vacio"),
  orderTotalElement: document.querySelector(".total-panel strong"),
  confirmOrderButton: document.querySelector(".confirmar-pedido"),
  checkoutView: document.querySelector(".checkout-vista"),
  checkoutLines: document.querySelector(".checkout-lineas"),
  checkoutTotal: document.querySelector("#checkout-total"),
  finalizeCheckoutButton: document.querySelector("#finalizar-checkout"),
  backToSummaryButton: document.querySelector(".volver-resumen"),
  confirmReturnOverlay: document.querySelector("#confirm-volver-overlay"),
  cancelReturnButton: document.querySelector("#cancelar-volver"),
  acceptReturnButton: document.querySelector("#confirmar-volver"),
  tableLabel: document.querySelector("#mesa-actual"),
};

export const tableNumber = new URLSearchParams(window.location.search).get("mesa");

export const state = {
  selectedProductCard: null,
  productQuantity: 1,
  orderQuantity: 0,
  orderTotal: 0,
  orderItems: [],
  selectedOrderItem: null,
  expandedOrderIndex: null,
  unsavedOrderIndex: null,
  toastTimer: null,
  dragStartY: 0,
  dragDistance: 0,
  isDraggingOrderPanel: false,
};

export const productResetTimers = new WeakMap();

export const availableExtras = [
  { name: "Porción de Papa a la Francesa", price: 8000 },
  { name: "Porción de Papa Criolla", price: 8000 },
  { name: "Extra de Queso Gratinado", price: 5000 },
  { name: "Extra de Tocineta", price: 6000 },
  { name: "Extra de Carne", price: 9000 },
  { name: "Extra de Pollo", price: 9000 },
];

if (elements.tableLabel && /^\d+$/.test(tableNumber || "")) {
  elements.tableLabel.textContent = `M. ${tableNumber}`;
}
