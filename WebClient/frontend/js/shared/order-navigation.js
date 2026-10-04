export function goToOrderStatus(tableNumber, orderNumber, location = window.location) {
  location.href = `status.html?mesa=${encodeURIComponent(tableNumber)}&id=${encodeURIComponent(orderNumber)}`;
}
