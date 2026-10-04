// Valores por defecto + lo que el usuario defina en window.RESTAURANT_CONFIG.
const DEFAULTS = {
  apiBaseUrl: "",
  // Cuánto espera el navegador la respuesta antes de rendirse.
  // Consultas (mesas, menú, estado del pedido). Antes eran 10 s.
  requestTimeoutMs: 30000,
  // Envío del pedido: la API hace varias llamadas RMI + consultas a la BD en cadena, así que puede tardar bastante más.
  orderTimeoutMs: 120000,
  statusPollIntervalMs: 10000,
  imageBaseUrl: "../assets/productos/",
  fallbackImage: "../assets/logo.png",
  endpoints: {
    mesas: "/api/mesas",
    productos: "/api/productos",
    adicionales: "/api/adicionales",
    pedidos: "/api/pedidos",
    estadoPedido: "/api/pedidos/{id}/estado",
  },
  paymentMethods: ["Efectivo", "Nequi", "Transferencia"],
  // slug de categoría -> etiqueta. Si no existe, se usa el texto de `producto.categoria`.
  categoryLabels: {},
  // slugs de categorías que se tratan como bebidas (sin editor de ingredientes)
  drinkCategories: ["bebidas"],
  // Adicionales de respaldo cuando el API no expone /api/adicionales
  // (el esquema actual de la BD no guarda precios de adicionales).
  extras: [
    { name: "Porción de Papa a la Francesa", price: 8000 },
    { name: "Porción de Papa Criolla", price: 8000 },
    { name: "Extra de Queso Gratinado", price: 5000 },
    { name: "Extra de Tocineta", price: 6000 },
    { name: "Extra de Carne", price: 9000 },
    { name: "Extra de Pollo", price: 9000 },
  ],
};

export function getConfig() {
  const user = window.RESTAURANT_CONFIG || {};
  return {
    ...DEFAULTS,
    ...user,
    endpoints: { ...DEFAULTS.endpoints, ...(user.endpoints || {}) },
    categoryLabels: { ...DEFAULTS.categoryLabels, ...(user.categoryLabels || {}) },
  };
}

export const isApiConfigured = () => Boolean(getConfig().apiBaseUrl);
