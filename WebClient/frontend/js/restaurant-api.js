const STATUS_VALUES = ["EN_COLA", "EN_PREPARACION", "LISTO"];

function getApiBaseUrl() {
  return (window.RESTAURANT_CONFIG?.apiBaseUrl || "").replace(/\/$/, "");
}

function normalizeStatus(status) {
  const value = String(status || "EN_COLA").toUpperCase();
  if (["PENDIENTE", "PENDING"].includes(value)) return "EN_COLA";
  if (["PREPARANDO", "PREPARACION", "PREPARING"].includes(value)) return "EN_PREPARACION";
  return STATUS_VALUES.includes(value) ? value : "EN_COLA";
}

export function normalizeOrderNumber(value = "000") {
  return String(value).replace(/^(?:PE-?)+/i, "");
}

export function createOrderPayload(items, tableNumber, paymentMethod) {
  return {
    mesa: Number(tableNumber || 1),
    metodoPago: paymentMethod,
    productos: items.map((item) => ({
      nombre: item.name,
      cantidad: item.quantity,
      precio: item.price,
      opcionBebida: item.drinkOption || null,
      ingredientesExcluidos: item.removedIngredients,
      adicionales: item.extraIngredients,
    })),
  };
}

export async function submitOrder(payload) {
  const apiBaseUrl = getApiBaseUrl();
  if (!apiBaseUrl) return { id: "000", estado: "EN_COLA", modo: "local" };

  const response = await fetch(`${apiBaseUrl}/api/pedidos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error("No fue posible registrar el pedido.");
  return normalizeOrder(await response.json());
}

export async function getOrderStatus(orderId) {
  const apiBaseUrl = getApiBaseUrl();
  if (!apiBaseUrl) return null;
  const backendOrderId = normalizeOrderNumber(orderId);
  const response = await fetch(`${apiBaseUrl}/api/pedidos/${encodeURIComponent(backendOrderId)}/estado`);
  if (!response.ok) throw new Error("No fue posible consultar el estado del pedido.");
  return normalizeOrder(await response.json());
}

export function normalizeOrder(order = {}) {
  const ordersInPreparation = Number(
    order.pedidosEnPreparacion ?? order.ordersInPreparation ?? order.enPreparacion ?? 0,
  );
  const requestedStatus = normalizeStatus(order.estado ?? order.status);
  return {
    id: order.id ?? order.orderId ?? order.pedidoId ?? order.idPedido,
    estado: requestedStatus,
    mesa: order.mesa ?? order.tableNumber,
    pedidosEnPreparacion: ordersInPreparation,
    posicionCola: Number(order.posicionCola ?? order.queuePosition ?? 1),
  };
}

export function isApiConfigured() {
  return Boolean(getApiBaseUrl());
}
