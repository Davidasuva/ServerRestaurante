import { getConfig } from "../config.js";

export class ApiError extends Error {
  constructor(message, { status = 0, body = null } = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

const messageFrom = (data) =>
  typeof data === "string" ? data : data?.message ?? data?.mensaje ?? data?.error ?? null;

/**
 * fetch JSON con timeout y errores normalizados (ApiError).
 * `timeoutMs` reemplaza al `requestTimeoutMs` global para una petición concreta (p. ej. el envío del pedido);
 * `timeoutMessage` permite explicar mejor qué pasó cuando se agota la espera.
 */
export async function request(path, { method = "GET", body, timeoutMs, timeoutMessage } = {}) {
  const { apiBaseUrl, requestTimeoutMs } = getConfig();
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs ?? requestTimeoutMs);
  try {
    const response = await fetch(`${apiBaseUrl.replace(/\/$/, "")}${path}`, {
      method,
      headers: { Accept: "application/json", ...(body !== undefined ? { "Content-Type": "application/json" } : {}) },
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });
    const text = await response.text();
    let data = null;
    if (text) { try { data = JSON.parse(text); } catch { data = text; } }
    if (!response.ok) throw new ApiError(messageFrom(data) || `Error del servidor (${response.status}).`, { status: response.status, body: data });
    return data;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    if (error.name === "AbortError") throw new ApiError(timeoutMessage || "El servidor tardó demasiado en responder.");
    throw new ApiError("No hay conexión con el servidor.");
  } finally {
    clearTimeout(timer);
  }
}
