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
