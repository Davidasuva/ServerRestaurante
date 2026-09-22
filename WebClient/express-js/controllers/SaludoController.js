const BridgeService = require('../services/BridgeService');
const SaludoModel = require('../models/SaludoModel');

/**
 * Controller: recibe req/res, delega en el service, formatea con el
 * model y responde. Sin lógica de fetch ni de RMI aquí — esa
 * responsabilidad vive en services/ y en el bridge Java.
 */
class SaludoController {
  static async saludar(req, res) {
    try {
      const nombre = req.query.nombre || 'Mundo';
      const bridgeData = await BridgeService.saludar(nombre);
      const saludo = SaludoModel.fromBridgeResponse(bridgeData);

      res.json(saludo.toJSON());
    } catch (err) {
      res.status(err.status || 500).json({
        error: err.message,
        detalle: err.detalle,
      });
    }
  }
}

module.exports = SaludoController;
