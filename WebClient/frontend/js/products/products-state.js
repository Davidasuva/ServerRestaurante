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
