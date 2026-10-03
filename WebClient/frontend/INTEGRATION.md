# Integracion con Spring Boot

El frontend funciona en modo local mientras `apiBaseUrl` este vacio. Para conectar el backend, modifica `js/restaurant-config.js`:

```js
window.RESTAURANT_CONFIG = {
  apiBaseUrl: "http://localhost:8080",
};
```

## Contrato de pedidos

`POST /api/pedidos`

```json
{
  "mesa": 4,
  "metodoPago": "Efectivo",
  "productos": [
    {
      "nombre": "Hamburguesa Rustica",
      "cantidad": 1,
      "precio": 28000,
      "opcionBebida": null,
      "ingredientesExcluidos": [],
      "adicionales": []
    }
  ]
}
```

La respuesta debe incluir el numero generado por la base de datos como `id`, `orderId`, `pedidoId` o `idPedido`, y opcionalmente `estado`. El frontend muestra ese valor con el prefijo fijo `PE-` y tres digitos (por ejemplo, la respuesta `1` se muestra como `PE-001`). El backend no debe enviar ni generar el prefijo visual.

## Contrato de estado

`GET /api/pedidos/{id}/estado`

```json
{
  "id": 101,
  "estado": "EN_COLA",
  "mesa": 4,
  "pedidosEnPreparacion": 2,
  "posicionCola": 3
}
```

Estados aceptados por la vista: `EN_COLA`, `EN_PREPARACION` y `LISTO`. Tambien se normalizan `PENDIENTE` y `PREPARANDO`. Para bebidas con alternativas, `opcionBebida` contiene una sola seleccion, por ejemplo `Agua` o `Leche entera`.

## Eventos Web Components

El checkout emite `restaurant-order-submit` con el payload del pedido. El componente `<order-status>` expone `setState(estado, detalles)` y emite `order-status-change` y `order-status-error`.
