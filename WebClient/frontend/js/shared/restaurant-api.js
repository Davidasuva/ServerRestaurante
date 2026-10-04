const STATUS_VALUES = ["PENDIENTE", "EN_COLA", "PREPARANDOSE", "PREPARADO", "ENTREGADO", "CANCELADO"];

function getApiBaseUrl() {
  return (window.RESTAURANT_CONFIG?.apiBaseUrl || "").replace(/\/$/, "");
}

function normalizeStatus(status) {
  if (status === undefined || status === null || status === "") return null;
  const value = String(status).toUpperCase();
  if (["PENDING"].includes(value)) return "PENDIENTE";
  if (["PREPARANDO", "PREPARACION", "PREPARING", "EN_PREPARACION"].includes(value)) return "PREPARANDOSE";
  if (["LISTO", "PREPARADO"].includes(value)) return "PREPARADO";
  return STATUS_VALUES.includes(value) ? value : null;
}

export function normalizeOrderNumber(value) {
  return String(value).replace(/^(?:PE-?)+/i, "");
}

export function createOrderPayload(items, tableNumber, paymentMethod) {
  const mesa = Number(tableNumber);
  if (!Number.isInteger(mesa) || mesa < 1) {
    throw new Error("No se ha identificado una mesa válida para el pedido.");
  }
  return {
    mesa,
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
  if (!apiBaseUrl) throw new Error("El backend no está configurado.");

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
  const ordersInPreparationValue =
    order.pedidosEnPreparacion ?? order.ordersInPreparation ?? order.enPreparacion;
  const ordersInPreparation = ordersInPreparationValue === undefined || ordersInPreparationValue === null
    ? null
    : Number(ordersInPreparationValue);
  const requestedStatus = normalizeStatus(order.estado ?? order.status);
  return {
    id: order.id ?? order.orderId ?? order.pedidoId ?? order.idPedido,
    estado: requestedStatus,
    mesa: order.mesa ?? order.tableNumber,
    pedidosEnPreparacion: ordersInPreparation,
    posicionCola: order.posicionCola ?? order.queuePosition ?? null,
  };
}

export function isApiConfigured() {
  return Boolean(getApiBaseUrl());
}
