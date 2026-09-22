/**
 * "Modelo" del recurso Saludo. En una app Express que consume un backend
 * externo (el bridge RMI), el modelo no habla con una BD, pero sigue
 * cumpliendo su rol: define la forma de los datos y cualquier
 * validación/transformación asociada a ellos.
 */
class SaludoModel {
  constructor({ resultado }) {
    this.resultado = resultado;
  }

  static fromBridgeResponse(data) {
    return new SaludoModel({ resultado: data.resultado });
  }

  toJSON() {
    return { resultado: this.resultado };
  }
}

module.exports = SaludoModel;
