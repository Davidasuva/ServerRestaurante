# Carbón y Sazón – Frontend (Web Components)

JavaScript nativo (módulos ES + Custom Elements, Light DOM, sin build). Lo sirve el backend Spring Boot (`../backend`),
que además expone la API en `/api`. Ver el README de la raíz para el despliegue y `../API.md` para el contrato.

```
html/        index.html (mesas) · products.html (menú) · status.html (seguimiento)
css/         estilos + components.css (ajustes de los componentes)
data/        *.mock.json  → solo para el modo demo sin backend (apiBaseUrl = "")
js/          restaurant-config.js (único archivo a editar) · config.js · services/ · store/ · components/ · pages/
```

Componentes: `<table-selector>`, `<menu-header>`, `<product-grid>`, `<product-card>`, `<product-modal>`, `<order-bar>`,
`<order-panel>`, `<order-list>`, `<checkout-view>`, `<confirm-dialog>`, `<app-toast>`, `<order-status>`.
Los datos de la API se escapan antes de pintarse (`utils/format.js → esc`).

## Estados del pedido (`<order-status>`)

| Servidor (`Pedido.Estado`) | API | Pantalla |
|---|---|---|
| `PENDIENTE` | `PENDIENTE` | Pedido recibido, en espera de que el personal lo confirme (punto pulsante en el paso 1) |
| `EN_COLA` | `EN_COLA` | En cola (punto pulsante en el paso 1) |
| `PREPARANDOSE` | `PREPARANDOSE` | En preparación (barra al 50 %) |
| `PREPARADO` | `PREPARADO` | Listo: todo verde y botón «Confirmar pedido recibido» |
| `ENTREGADO` | `ENTREGADO` | Entregado (deja de consultar) |
| `CANCELADO` | `CANCELADO` | Todo en rojo, el último paso pasa a «CANCELADO» (deja de consultar) |

Un pedido creado desde la web nace como `PENDIENTE`. La fila de espera (`posicionCola`) cuenta los pedidos `PENDIENTE` y `EN_COLA`; `pedidosEnPreparacion` cuenta los `PREPARANDOSE`.

La configuración de textos/iconos está en `js/services/order-status-config.js`; los sinónimos aceptados en `normalizeStatus` (`js/services/mappers.js`).
El CSS de cada paso usa los ids `#step-cola`, `#step-preparandose` y `#step-entregado`: si cambias el HTML del componente, conserva esos ids.
