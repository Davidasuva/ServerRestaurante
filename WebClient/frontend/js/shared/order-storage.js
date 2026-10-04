const ORDER_ID_KEY = "currentOrderId";
const ORDER_TABLE_KEY = "currentOrderMesa";

export function saveCurrentOrder(orderId, tableNumber, storage = window.sessionStorage) {
  storage.setItem(ORDER_ID_KEY, orderId);
  storage.setItem(ORDER_TABLE_KEY, tableNumber);
}

export function getCurrentOrder(storage = window.sessionStorage) {
  return {
    id: storage.getItem(ORDER_ID_KEY),
    table: storage.getItem(ORDER_TABLE_KEY),
  };
}
