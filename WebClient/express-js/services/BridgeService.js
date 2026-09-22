const BRIDGE_URL = process.env.BRIDGE_URL || 'http://localhost:8081';

/**
 * Capa de "servicio": encapsula la comunicación HTTP con el bridge Java
 * (que a su vez habla RMI). Los controllers nunca hacen fetch()
 * directamente, siempre pasan por aquí. Así, si mañana cambias el
 * bridge por otra cosa, solo tocas este archivo.
 */
class BridgeService {
  static async saludar(nombre) {
    const response = await fetch(`${BRIDGE_URL}/rmi/saludar?nombre=${encodeURIComponent(nombre)}`);

    if (!response.ok) {
      const errBody = await response.json().catch(() => ({}));
      const error = new Error(errBody.error || 'Error en el bridge RMI');
      error.status = 502;
      error.detalle = errBody;
      throw error;
    }

    return response.json();
  }

  // Añade aquí un método por cada operación remota expuesta por el bridge,
  // por ejemplo: static async obtenerUsuarios() { ... }
}

module.exports = BridgeService;
