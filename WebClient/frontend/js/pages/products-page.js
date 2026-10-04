import "../components/index.js";
import { showToast } from "../components/app-toast.js";
import { getExtras, getMenu } from "../services/restaurant-api.js";
import { cartStore } from "../store/cart-store.js";

const params = new URLSearchParams(window.location.search);
const tableParam = params.get("mesa");
const tableId = /^\d+$/.test(tableParam || "") ? tableParam : "1";

const header = document.querySelector("menu-header");
const grid = document.querySelector("product-grid");
const modal = document.querySelector("product-modal");
const panel = document.querySelector("order-panel");
header.setAttribute("table", tableId);
panel.setAttribute("table", tableId);

// Evita que el botón "atrás" saque al cliente del menú.
const productUrl = window.location.href;
window.history.replaceState({ productPage: true }, "", productUrl);
window.history.pushState({ productPageGuard: true }, "", productUrl);
window.addEventListener("popstate", () => window.history.pushState({ productPageGuard: true }, "", productUrl));

function addToCart(product, quantity = 1) {
  if (!cartStore.add(product, quantity)) {
    showToast("El pedido ya fue enviado a cocina", "error");
    return false;
  }
  return true;
}

header.addEventListener("menu-filter-change", (e) => grid.applyFilter(e.detail));
grid.addEventListener("product-select", (e) => modal.open(e.detail.product));
grid.addEventListener("product-add", (e) => { if (!addToCart(e.detail.product)) e.preventDefault(); });
grid.addEventListener("catalog-retry", loadMenu);
modal.addEventListener("product-confirm", (e) => {
  if (addToCart(e.detail.product, e.detail.quantity)) grid.markAdded(e.detail.product.id);
});

async function loadMenu() {
  grid.showLoading();
  try {
    const [{ products, categories }, extras] = await Promise.all([getMenu(), getExtras()]);
    cartStore.setExtras(extras);
    header.categories = categories;
    grid.setProducts(products);
  } catch (error) {
    grid.showError(error.message || "No se pudo cargar el menú.");
  }
}

loadMenu();
