// Configuración editable del frontend (se carga como script clásico antes de los módulos).
//
// apiBaseUrl:
//   window.location.origin → la página la sirve el mismo Spring Boot que expone /api (configuración por defecto).
//   "http://192.168.1.50:8080" → API en otro equipo (hay que permitir CORS: app.cors.allowed-origins).
//   ""                     → MODO LOCAL de demostración con los datos de /data/*.mock.json (sin backend).
window.RESTAURANT_CONFIG = {
  apiBaseUrl: window.location.origin,
};
