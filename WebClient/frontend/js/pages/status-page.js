import "../components/index.js";

// Compatibilidad: el backend/websocket puede avisar cambios con un evento de documento.
document.addEventListener("restaurant-order-status", (event) => {
  const d = event.detail || {};
  document.querySelector("order-status")?.setState(d.estado || d.state, {
    ordersInPreparation: d.pedidosEnPreparacion ?? d.ordersInPreparation,
    queuePosition: d.posicionCola ?? d.queuePosition,
  });
});

window.orderStatus = document.querySelector("order-status");
