import { getConfig, isApiConfigured } from "../config.js";
import { readStorage, writeStorage } from "../utils/format.js";
import { request } from "./http-client.js";
import { deriveCategories, mapExtra, mapProduct, mapTable, normalizeOrder, normalizeOrderNumber } from "./mappers.js";

export { isApiConfigured, normalizeOrderNumber };

const asList = (data) => (Array.isArray(data) ? data : data?.content ?? data?.data ?? []);

/** En modo local se leen los JSON de /data (misma forma que devolvería el API). */
async function loadMock(file) {
  const response = await fetch(new URL(`../../data/${file}`, import.meta.url));
  if (!response.ok) throw new Error(`No se pudo cargar ${file}`);
  return response.json();
}

export async function getTables() {
  const { endpoints } = getConfig();
  const data = isApiConfigured() ? await request(endpoints.mesas) : await loadMock("mesas.mock.json");
  return asList(data).map(mapTable).sort((a, b) => a.id - b.id);
}

export async function getMenu() {
  const { endpoints } = getConfig();
  const data = isApiConfigured() ? await request(endpoints.productos) : await loadMock("menu.mock.json");
  const products = asList(data).map(mapProduct);
  return { products, categories: deriveCategories(products) };
}

/** Adicionales: /api/adicionales si existe; si no, los definidos en la configuración. */
export async function getExtras() {
  const { endpoints, extras } = getConfig();
  if (isApiConfigured()) {
    try {
      const list = asList(await request(endpoints.adicionales)).map(mapExtra);
      if (list.length) return list;
    } catch (error) {
      if (error.status !== 404) console.warn("No se pudieron cargar los adicionales; se usan los de la configuración.", error);
    }
  }
  return extras.map(mapExtra);
}

export async function submitOrder(payload) {
  if (!isApiConfigured()) {
    const next = Number(readStorage(localStorage, "mockOrderSeq") || 0) + 1;
    writeStorage(localStorage, "mockOrderSeq", String(next));
    return { id: String(next), estado: "EN_COLA", mesa: payload.idMesa, pedidosEnPreparacion: 0, posicionCola: 1, modo: "local" };
  }
  const { endpoints, orderTimeoutMs } = getConfig();
  const data = await request(endpoints.pedidos, {
    method: "POST",
    body: payload,
    timeoutMs: orderTimeoutMs,
    // Si se agota la espera, el servidor pudo haber guardado el pedido igual: avisar antes de que el cliente reenvíe.
    timeoutMessage: "El servidor tardó demasiado en responder. Es posible que el pedido sí se haya registrado: consulta con el personal antes de volver a enviarlo.",
  });
  const order = normalizeOrder(data ?? {});
  if (order.id === undefined || order.id === null) throw new Error("El servidor no devolvió el número del pedido.");
  return order;
}

export async function getOrderStatus(orderId) {
  if (!isApiConfigured()) return null;
  const path = getConfig().endpoints.estadoPedido.replace("{id}", encodeURIComponent(normalizeOrderNumber(orderId)));
  return normalizeOrder(await request(path));
}
