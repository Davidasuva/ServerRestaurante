const express = require('express');
const router = express.Router();
const SaludoController = require('../controllers/SaludoController');

// GET /api/saludar?nombre=Juan
router.get('/saludar', SaludoController.saludar);

// Añade aquí una línea por cada nuevo recurso/controller
// router.get('/usuarios', UsuarioController.listar);

module.exports = router;
