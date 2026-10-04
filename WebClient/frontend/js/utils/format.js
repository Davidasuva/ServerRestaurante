const ESCAPES = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" };

/** Escapa texto para usarlo dentro de plantillas innerHTML (los datos ahora vienen de la API). */
export const esc = (value) => String(value ?? "").replace(/[&<>"']/g, (c) => ESCAPES[c]);

export const formatCOP = (value) => `$${Number(value || 0).toLocaleString("es-CO")}`;

export const slugify = (text) =>
  String(text ?? "")
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");

export const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export const pick = (obj, ...keys) => {
  for (const key of keys) if (obj?.[key] !== undefined && obj[key] !== null) return obj[key];
  return undefined;
};

export function readStorage(storage, key) {
  try { return storage.getItem(key); } catch { return null; }
}
export function writeStorage(storage, key, value) {
  try { storage.setItem(key, value); } catch { /* almacenamiento no disponible */ }
}
