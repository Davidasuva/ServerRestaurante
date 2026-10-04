import { checkoutElements } from "./checkout-elements.js";
import { state } from "./products-state.js";
import { tableNumber } from "../tables/table-context.js";
import { showToast } from "./products-ui.js";
import { createOrderPayload, normalizeOrderNumber, submitOrder } from "../shared/restaurant-api.js";
import { bindProductViewEvents } from "./products-events.js";
import { initializeProductLoading } from "./products-loading.js";
import { lockProductNavigation } from "./products-navigation.js";
import { setViewState } from "../shared/view-state.js";
import { saveCurrentOrder } from "../shared/order-storage.js";
import { goToOrderStatus } from "../shared/order-navigation.js";

const {
  checkoutView,
  finalizeCheckoutButton,
  backToSummaryButton,
  paymentFieldset,
  paymentInputs,
} = checkoutElements;

async function sendOrderToKitchen() {
  const paymentMethod = document.querySelector('input[name="metodo-pago"]:checked').value;
  const currentMesa = tableNumber;
  const payload = createOrderPayload(state.orderItems, currentMesa, paymentMethod);

  document.dispatchEvent(new CustomEvent("restaurant-order-submit", {
    detail: payload,
  }));

  finalizeCheckoutButton.disabled = true;
  finalizeCheckoutButton.textContent = "Pedido enviado";
  backToSummaryButton.disabled = true;
  if (paymentFieldset) paymentFieldset.disabled = true;
  paymentInputs.forEach((input) => { input.disabled = true; });
  checkoutView.classList.add("pedido-confirmado");
  setViewState(checkoutView, "loading");
  try {
    const order = await submitOrder(payload);
    if (!order.id) throw new Error("El backend no devolvió el identificador del pedido.");
    const orderNumber = normalizeOrderNumber(order.id);
    saveCurrentOrder(orderNumber, currentMesa);
    setViewState(checkoutView, "success");
    showToast(`Pedido enviado a cocina · ${paymentMethod}`, "success");
    setTimeout(() => goToOrderStatus(currentMesa, orderNumber), 1600);
  } catch (error) {
    finalizeCheckoutButton.disabled = false;
    finalizeCheckoutButton.textContent = "Enviar pedido a cocina";
    backToSummaryButton.disabled = false;
    checkoutView.classList.remove("pedido-confirmado");
    setViewState(checkoutView, "error");
    showToast(error.message, "error");
  }
}

finalizeCheckoutButton.addEventListener("click", sendOrderToKitchen);
lockProductNavigation();
bindProductViewEvents();
initializeProductLoading();
