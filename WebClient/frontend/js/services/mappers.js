import { getConfig } from "../config.js";
import { pick, slugify } from "../utils/format.js";

const STATUS_VALUES = ["EN_COLA", "EN_PREPARACION", "LISTO", "CANCELADO"];

/* ---------- Respuestas del API -> modelos de la vista ---------- */

export function mapTable(raw) {
  return { id: Number(typeof raw === "object" ? pick(raw, "id", "idMesa", "id_mesa") : raw) };
}

function mapIngredient(raw) {
  if (typeof raw === "string") return { id: null, name: raw, base: false };
  return {
    id: pick(raw, "id", "idIngrediente", "id_ingrediente") ?? null,
    name: pick(raw, "nombre", "name") ?? "",
    // El esquema actual no marca ingredientes base: el backend puede enviar `base`/`fijo`.
    base: Boolean(pick(raw, "base", "fijo", "esBase", "obligatorio")),
  };
}

const toList = (value) =>
  Array.isArray(value) ? value : String(value ?? "").split(",").map((s) => s.trim()).filter(Boolean);

export function resolveImage(raw, id) {
  const { imageBaseUrl } = getConfig();
  const image = pick(raw, "imagen", "image");
  if (!image) return `${imageBaseUrl}${id}.jpg`;
  return /^(https?:|data:|\/)/i.test(image) ? image : `${imageBaseUrl}${image}`;
}

export function mapProduct(raw) {
  const { categoryLabels } = getConfig();
  const id = pick(raw, "id", "idProducto", "id_producto");
  const categoryName = String(pick(raw, "categoria", "category") ?? "");
  const categoryId = slugify(categoryName);
  return {
    id,
    name: pick(raw, "nombre", "name") ?? "",
    description: pick(raw, "descripcion", "description") ?? "",
    price: Number(pick(raw, "precio", "price") ?? 0),
    image: resolveImage(raw, id),
    category: { id: categoryId, label: categoryLabels[categoryId] ?? categoryName },
    ingredients: toList(raw.ingredientes).map(mapIngredient),
    drinkOptions: toList(pick(raw, "opcionesBebida", "opciones_bebida")).map(String),
  };
}

export function mapExtra(raw) {
  return {
    id: pick(raw, "id", "idIngrediente", "id_ingrediente") ?? null,
    name: pick(raw, "nombre", "name"),
    price: Number(pick(raw, "precio", "price") ?? 0),
  };
}

/** Categorías únicas en el orden en que aparecen los productos. */
export function deriveCategories(products) {
  const seen = new Map();
  products.forEach((p) => { if (p.category.id && !seen.has(p.category.id)) seen.set(p.category.id, p.category); });
  return [...seen.values()];
}

/* ---------- Pedidos ---------- */

export function normalizeStatus(status) {
  const value = String(status || "EN_COLA").toUpperCase().replace(/\s+/g, "_");
  if (["PENDIENTE", "PENDING"].includes(value)) return "EN_COLA";
  if (["PREPARANDO", "PREPARACION", "PREPARING"].includes(value)) return "EN_PREPARACION";
  if (["ENTREGADO", "DELIVERED", "TERMINADO"].includes(value)) return "LISTO";
  if (["CANCELADA", "CANCELLED", "CANCELED"].includes(value)) return "CANCELADO";
  return STATUS_VALUES.includes(value) ? value : "EN_COLA";
}

export function normalizeOrderNumber(value = "000") {
  return String(value).replace(/^(?:PE-?)+/i, "");
}

export function normalizeOrder(order = {}) {
  return {
    id: pick(order, "id", "orderId", "pedidoId", "idPedido"),
    estado: normalizeStatus(pick(order, "estado", "status")),
    mesa: pick(order, "mesa", "idMesa", "id_mesa", "tableNumber"),
    pedidosEnPreparacion: Number(pick(order, "pedidosEnPreparacion", "ordersInPreparation", "enPreparacion") ?? 0),
    posicionCola: Number(pick(order, "posicionCola", "queuePosition") ?? 1),
  };
}

/**
 * Carrito -> cuerpo de POST /api/pedidos (alineado con las tablas pedido, producto_pedido e inventario_pedido).
 * El backend asigna `id`, `fecha_pedido` y el `estado` inicial.
 * Los adicionales NO se envían: se muestran en la pantalla pero no se guardan en la BD.
 */
export function buildOrderPayload({ items, tableId, method, total }) {
  return {
    idMesa: Number(tableId),
    metodo: method,
    precioTotal: total,
    productos: items.map((item) => ({
      idProducto: item.product.id,
      nombre: item.name,
      cantidad: item.quantity,
      precio: item.price,
      opcionBebida: item.drinkOption || null,
      ingredientesExcluidos: item.removedIngredients.map((name) => {
        const ingredient = item.product.ingredients.find((i) => i.name === name);
        return { id: ingredient?.id ?? null, nombre: name };
      }),
    })),
  };
}
