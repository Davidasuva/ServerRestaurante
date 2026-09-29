const filterOptions = document.querySelectorAll(".filtro-opcion");
const productCards = document.querySelectorAll(".producto-card");
const searchInput = document.querySelector(".buscador-productos input");
const addProductButtons = document.querySelectorAll(".agregar-producto");
const orderButton = document.querySelector(".pedido");
const orderOverlay = document.querySelector(".pedido-overlay");
const orderPanel = document.querySelector(".panel-pedido");
const orderDragHandle = document.querySelector(".arrastre-pedido");
const backToMenuButton = document.querySelector(".volver-menu");
const productOverlay = document.querySelector(".producto-overlay");
const productModal = document.querySelector(".producto-modal");
const closeProductButton = document.querySelector(".cerrar-producto");
const confirmProductButton = document.querySelector(".confirmar-producto");
const modalImage = document.querySelector(".producto-modal-imagen");
const modalTitle = document.querySelector("#titulo-producto-modal");
const modalPrice = document.querySelector(".producto-modal-precio");
const modalDescription = document.querySelector(".producto-modal-descripcion");
const orderList = document.querySelector(".lista-pedido");
const emptyOrderState = document.querySelector(".pedido-vacio");
const orderTotalElement = document.querySelector(".total-panel strong");
const confirmOrderButton = document.querySelector(".confirmar-pedido");
const checkoutView = document.querySelector(".checkout-vista");
const checkoutLines = document.querySelector(".checkout-lineas");
const checkoutTotal = document.querySelector("#checkout-total");
const finalizeCheckoutButton = document.querySelector("#finalizar-checkout");
const backToSummaryButton = document.querySelector(".volver-resumen");
const confirmReturnOverlay = document.querySelector("#confirm-volver-overlay");
const cancelReturnButton = document.querySelector("#cancelar-volver");
const acceptReturnButton = document.querySelector("#confirmar-volver");
const tableLabel = document.querySelector("#mesa-actual");

const tableNumber = new URLSearchParams(window.location.search).get("mesa");
if (tableLabel && /^\d+$/.test(tableNumber || "")) {
  tableLabel.textContent = `M. ${tableNumber}`;
}

let selectedProductCard;
let productQuantity = 1;
let orderQuantity = 0;
let orderTotal = 0;
let orderItems = [];
let selectedOrderItem;
let selectedIngredients = [];
let extraIngredients = [];
let expandedOrderIndex = null;
let unsavedOrderIndex = null;
let toastTimer;
let dragStartY = 0;
let dragDistance = 0;
let isDraggingOrderPanel = false;
const productResetTimers = new WeakMap();

const availableExtras = [
  { name: "Porción de Papa a la Francesa", price: 8000 },
  { name: "Porción de Papa Criolla", price: 8000 },
  { name: "Extra de Queso Gratinado", price: 5000 },
  { name: "Extra de Tocineta", price: 6000 },
  { name: "Extra de Carne o Pollo", price: 9000 },
];

function setOrderPanelOpen(isOpen) {
  if (isOpen) orderPanel.style.transform = "";
  orderOverlay.classList.toggle("abierto", isOpen);
  orderOverlay.setAttribute("aria-hidden", String(!isOpen));
  orderButton.setAttribute("aria-expanded", String(isOpen));
  document.body.classList.toggle("pedido-abierto", isOpen);
}

function startOrderDrag(clientY) {
  isDraggingOrderPanel = true;
  dragStartY = clientY;
  dragDistance = 0;
  orderPanel.classList.add("arrastrando");
}

function moveOrderDrag(clientY) {
  if (!isDraggingOrderPanel) return;
  dragDistance = Math.max(0, clientY - dragStartY);
  orderPanel.style.transform = `translateY(${dragDistance}px)`;
}

function finishOrderDrag() {
  if (!isDraggingOrderPanel) return;
  isDraggingOrderPanel = false;
  orderPanel.classList.remove("arrastrando");
  if (dragDistance > 100 && ensureNoUnsavedChanges()) {
    setOrderPanelOpen(false);
  } else {
    orderPanel.style.transform = "";
  }
  dragDistance = 0;
}

orderDragHandle.addEventListener("mousedown", (event) => {
  event.preventDefault();
  startOrderDrag(event.clientY);
});
document.addEventListener("mousemove", (event) => moveOrderDrag(event.clientY));
document.addEventListener("mouseup", finishOrderDrag);
orderDragHandle.addEventListener(
  "touchstart",
  (event) => {
    event.preventDefault();
    startOrderDrag(event.touches[0].clientY);
  },
  { passive: false },
);
document.addEventListener(
  "touchmove",
  (event) => {
    if (isDraggingOrderPanel) moveOrderDrag(event.touches[0].clientY);
  },
  { passive: false },
);
document.addEventListener("touchend", finishOrderDrag);
document.addEventListener("touchcancel", finishOrderDrag);

function setProductModalOpen(isOpen) {
  productOverlay.classList.toggle("abierto", isOpen);
  productOverlay.setAttribute("aria-hidden", String(!isOpen));
  document.body.classList.toggle("producto-abierto", isOpen);
}

function parseProductPrice(card) {
  return Number(
    card.querySelector(".producto-precio").textContent.replace(/[^0-9]/g, ""),
  );
}

function updateOrderSummary() {
  orderButton.classList.toggle("pedido-con-productos", orderItems.length > 0);
  document.querySelector(".cantidad-pedido").textContent = orderQuantity;
  document.querySelector(".total-pedido").textContent =
    `$${orderTotal.toLocaleString("es-CO")}`;
  orderTotalElement.textContent = `$${orderTotal.toLocaleString("es-CO")}`;
  emptyOrderState.hidden = orderItems.length > 0;
  if (orderItems.length > 0) emptyOrderState.classList.remove("apareciendo");
  orderList.hidden = orderItems.length === 0;
  confirmOrderButton.disabled = orderItems.length === 0;
}

function getOrderItemTotal(item) {
  const extrasTotal = item.extraIngredients.reduce((total, ingredient) => {
    const extraName =
      typeof ingredient === "string" ? ingredient : ingredient.name;
    const extraQuantity =
      typeof ingredient === "string" ? 1 : ingredient.quantity;
    const extra = availableExtras.find(
      (availableExtra) => availableExtra.name === extraName,
    );
    return total + (extra?.price || 0) * extraQuantity;
  }, 0);
  return (item.price + extrasTotal) * item.quantity;
}

function renderCheckout() {
  checkoutLines.innerHTML = orderItems
    .map(
      (item) => `
        <div class="checkout-linea">
            <img src="${item.image}" alt="">
            <span class="checkout-linea-info"><strong>${item.name}</strong><small>${item.quantity} unidad${item.quantity === 1 ? "" : "es"}</small></span>
            <strong class="checkout-linea-precio">$${getOrderItemTotal(item).toLocaleString("es-CO")}</strong>
        </div>
    `,
    )
    .join("");
  checkoutTotal.textContent = `$${orderTotal.toLocaleString("es-CO")}`;
}

function showCheckout() {
  setConfirmReturnOpen(false);
  renderCheckout();
  finalizeCheckoutButton.disabled = false;
  finalizeCheckoutButton.textContent = "Enviar pedido a cocina";
  backToSummaryButton.disabled = false;
  const paymentFieldset = document.querySelector(".checkout-payment");
  if (paymentFieldset) paymentFieldset.disabled = false;
  document.querySelectorAll('input[name="metodo-pago"]').forEach((input) => {
    input.disabled = false;
  });
  checkoutView.classList.remove("pedido-confirmado");
  orderList.hidden = true;
  emptyOrderState.hidden = true;
  document.querySelector(".panel-pedido-footer").hidden = true;
  checkoutView.hidden = false;
  showToast("Pedido confirmado", "success");
}

function showOrderSummary() {
  if (backToSummaryButton.disabled) return;
  setConfirmReturnOpen(false);
  checkoutView.hidden = true;
  document.querySelector(".panel-pedido-footer").hidden = false;
  updateOrderSummary();
}

function setConfirmReturnOpen(isOpen) {
  if (!confirmReturnOverlay) return;
  if (isOpen) {
    confirmReturnOverlay.hidden = false;
    requestAnimationFrame(() => {
      confirmReturnOverlay.classList.add("abierto");
      confirmReturnOverlay.setAttribute("aria-hidden", "false");
    });
  } else {
    confirmReturnOverlay.classList.remove("abierto");
    confirmReturnOverlay.setAttribute("aria-hidden", "true");
    setTimeout(() => {
      if (!confirmReturnOverlay.classList.contains("abierto")) {
        confirmReturnOverlay.hidden = true;
      }
    }, 250);
  }
}

function renderOrderItems(animateEditor = false) {
  orderList.innerHTML = orderItems
    .map(
      (item, index) => `
        <div class="pedido-item ${expandedOrderIndex === index ? "expandido" : ""}" data-index="${index}">
            <div class="pedido-item-resumen" role="button" tabindex="0" aria-expanded="${expandedOrderIndex === index}">
                <img src="${item.image}" alt="${item.name}">
                <span class="pedido-item-info">
                    <strong>${item.name}</strong>
                    <span>$${item.price.toLocaleString("es-CO")}</span>
                </span>
                <span class="pedido-item-cantidad">x${item.quantity}</span>
                <button class="eliminar-pedido" type="button" aria-label="Eliminar ${item.name}">
                    <span class="material-symbols-outlined">delete</span>
                </button>
            </div>
            ${expandedOrderIndex === index ? renderOrderEditor(item, index, animateEditor) : ""}
        </div>
    `,
    )
    .join("");
  orderList.querySelectorAll(".pedido-item-resumen").forEach((itemButton) => {
    const toggleItem = (event) => {
      if (event.target.closest(".eliminar-pedido")) return;
      const index = Number(itemButton.closest(".pedido-item").dataset.index);
      if (expandedOrderIndex === index) {
        collapseOrderItem(index);
        return;
      }
      openOrderItem(index);
    };
    itemButton.addEventListener("click", toggleItem);
    itemButton.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        toggleItem(event);
      }
    });
    itemButton
      .querySelector(".eliminar-pedido")
      .addEventListener("click", () => {
        removeOrderItem(
          Number(itemButton.closest(".pedido-item").dataset.index),
        );
      });
  });
  bindOrderEditor();
}

function renderOrderEditor(item, index, animateEditor) {
  const ingredients = item.card.dataset.ingredientes
    .split(",")
    .map((ingredient) => ingredient.trim());
  return `
        <div class="pedido-editor ${animateEditor ? "animar" : ""}" data-editor-index="${index}">
            <h3>Ingredientes</h3>
            <div class="ingredientes-lista">
                ${ingredients
                  .map(
                    (ingredient) => `
                    <label class="ingrediente-fila">
                        <input type="radio" data-ingrediente="${ingredient}" ${item.removedIngredients.includes(ingredient) ? "" : "checked"}>
                        <span>${ingredient}</span>
                    </label>
                `,
                  )
                  .join("")}
            </div>
            <h3>Adicionales</h3>
            <div class="adicionales-lista">
                ${availableExtras
                  .map(
                    (extra) => `
                    <div class="adicional-fila ${getExtraQuantity(item, extra.name) > 0 ? "seleccionado" : ""}" data-extra="${extra.name}">
                        <span class="adicional-info"><strong>${extra.name}</strong><small>+$${extra.price.toLocaleString("es-CO")}</small></span>
                        <span class="selector-adicional" aria-label="Cantidad de ${extra.name}">
                            <button class="cambiar-adicional disminuir-adicional" type="button" aria-label="Disminuir ${extra.name}" ${getExtraQuantity(item, extra.name) === 0 ? "disabled" : ""}>−</button>
                            <strong class="cantidad-adicional">${getExtraQuantity(item, extra.name)}</strong>
                            <button class="cambiar-adicional aumentar-adicional" type="button" aria-label="Aumentar ${extra.name}">+</button>
                        </span>
                    </div>
                `,
                  )
                  .join("")}
            </div>
            <div class="pedido-editor-footer">
                <button class="guardar-edicion" type="button">Guardar cambios</button>
            </div>
        </div>
    `;
}

function getExtraQuantity(item, extraName) {
  const extra = item.extraIngredients.find((ingredient) => {
    return typeof ingredient === "string"
      ? ingredient === extraName
      : ingredient.name === extraName;
  });
  return typeof extra === "string" ? 1 : extra?.quantity || 0;
}

function setExtraQuantity(item, extraName, quantity) {
  const extraIndex = item.extraIngredients.findIndex((ingredient) => {
    return typeof ingredient === "string"
      ? ingredient === extraName
      : ingredient.name === extraName;
  });
  if (quantity === 0) {
    if (extraIndex >= 0) item.extraIngredients.splice(extraIndex, 1);
    return;
  }
  if (extraIndex >= 0) {
    item.extraIngredients[extraIndex] = { name: extraName, quantity };
  } else {
    item.extraIngredients.push({ name: extraName, quantity });
  }
}

function collapseOrderItem(index) {
  if (!ensureNoUnsavedChanges(index)) return;
  const editor = orderList.querySelector(`[data-editor-index="${index}"]`);
  if (!editor) return;
  editor.classList.add("cerrando");
  setTimeout(() => {
    expandedOrderIndex = null;
    calculateOrderSummary();
  }, 380);
}

function openOrderItem(index) {
  if (!ensureNoUnsavedChanges()) return;
  if (expandedOrderIndex === null) {
    expandedOrderIndex = index;
    renderOrderItems(true);
    return;
  }

  const currentEditor = orderList.querySelector(".pedido-editor");
  if (!currentEditor) {
    expandedOrderIndex = index;
    renderOrderItems(true);
    return;
  }

  currentEditor.classList.add("cerrando");
  setTimeout(() => {
    expandedOrderIndex = index;
    renderOrderItems(true);
  }, 380);
}

function removeOrderItem(index) {
  if (!ensureNoUnsavedChanges(index)) return;
  const orderItem = orderList.querySelector(`[data-index="${index}"]`);
  if (!orderItem) return;
  orderItem.classList.add("eliminando");
  setTimeout(() => {
    orderItems.splice(index, 1);
    expandedOrderIndex = null;
    unsavedOrderIndex = null;
    calculateOrderSummary();
    if (orderItems.length === 0) animateEmptyOrderState();
  }, 320);
}

function ensureNoUnsavedChanges(index = null) {
  if (
    unsavedOrderIndex === null ||
    (index !== null && unsavedOrderIndex !== index)
  )
    return true;
  showToast("Guarda los cambios del pedido abierto antes de continuar.", "error");
  return false;
}

function animateEmptyOrderState() {
  emptyOrderState.classList.remove("preparando-entrada");
  emptyOrderState.classList.add("preparando-entrada");
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      emptyOrderState.classList.remove("preparando-entrada");
    });
  });
}

function showToast(message, type = "success") {
  let toast = document.querySelector(".pedido-toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.className = "pedido-toast";
    document.body.appendChild(toast);
  }
  toast.textContent = message;
  toast.classList.remove("success", "error");
  toast.classList.add(type);
  toast.classList.add("visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove("visible"), 3000);
}

function bindOrderEditor() {
  const editor = orderList.querySelector(".pedido-editor");
  if (!editor) return;
  const index = Number(editor.dataset.editorIndex);
  const item = orderItems[index];
  editor.querySelectorAll(".ingrediente-fila").forEach((row) => {
    const input = row.querySelector("input");
    row.addEventListener("click", (event) => {
      event.preventDefault();
      input.checked = !input.checked;
      unsavedOrderIndex = index;
    });
  });
  editor.querySelectorAll(".adicional-fila").forEach((row) => {
    const extra = row.dataset.extra;
    row.querySelector(".disminuir-adicional").addEventListener("click", () => {
      setExtraQuantity(
        item,
        extra,
        Math.max(0, getExtraQuantity(item, extra) - 1),
      );
      unsavedOrderIndex = index;
      calculateOrderSummary();
    });
    row.querySelector(".aumentar-adicional").addEventListener("click", () => {
      setExtraQuantity(item, extra, getExtraQuantity(item, extra) + 1);
      unsavedOrderIndex = index;
      calculateOrderSummary();
    });
  });
  editor.querySelector(".guardar-edicion").addEventListener("click", () => {
    item.removedIngredients = [
      ...editor.querySelectorAll(".ingrediente-fila input:not(:checked)"),
    ].map((input) => input.dataset.ingrediente);
    editor.querySelector(".guardar-edicion").disabled = true;
    unsavedOrderIndex = null;
    collapseOrderItem(index);
  });
}

function calculateOrderSummary() {
  orderQuantity = orderItems.reduce((total, item) => total + item.quantity, 0);
  orderTotal = orderItems.reduce(
    (total, item) => total + getOrderItemTotal(item),
    0,
  );
  renderOrderItems();
  updateOrderSummary();
}

function openProductModal(card, orderItem = null) {
  selectedProductCard = card;
  selectedOrderItem = orderItem;
  productQuantity = orderItem ? orderItem.quantity : 1;
  selectedIngredients = card.dataset.ingredientes
    .split(",")
    .map((ingredient) => ingredient.trim());
  extraIngredients = orderItem ? [...orderItem.extraIngredients] : [];
  modalImage.src = card.querySelector("img").src;
  modalImage.alt = card.querySelector("img").alt;
  modalTitle.textContent = card.querySelector("h2").textContent;
  modalPrice.textContent = card.querySelector(".producto-precio").textContent;
  modalDescription.textContent = card.dataset.descripcion;
  confirmProductButton.textContent = "Añadir al pedido";
  setProductModalOpen(true);
}

function updateProducts() {
  const selectedCategory = document.querySelector(".filtro-opcion.activo")
    .dataset.categoria;
  const searchText = searchInput.value.trim().toLowerCase();

  productCards.forEach((card) => {
    const matchesCategory =
      selectedCategory === "todas" ||
      card.dataset.categoria === selectedCategory;
    const matchesSearch = card.textContent.toLowerCase().includes(searchText);
    card.classList.toggle("oculto", !matchesCategory || !matchesSearch);
  });
}

filterOptions.forEach((option) => {
  option.addEventListener("click", () => {
    filterOptions.forEach((item) => item.classList.remove("activo"));
    option.classList.add("activo");
    updateProducts();
  });
});

searchInput.addEventListener("input", updateProducts);

productCards.forEach((card) => {
  card.addEventListener("click", (event) => {
    if (!event.target.closest(".agregar-producto")) {
      openProductModal(card);
    }
  });
});

closeProductButton.addEventListener("click", () => setProductModalOpen(false));

confirmProductButton.addEventListener("click", () => {
  if (finalizeCheckoutButton.disabled) {
    showToast("El pedido ya fue enviado a cocina", "error");
    setProductModalOpen(false);
    return;
  }
  const itemData = {
    card: selectedProductCard,
    name: selectedProductCard.querySelector("h2").textContent,
    image: selectedProductCard.querySelector("img").src,
    price: parseProductPrice(selectedProductCard),
    quantity: productQuantity,
    removedIngredients: [],
    extraIngredients: [],
  };
  if (selectedOrderItem) {
    Object.assign(selectedOrderItem, itemData);
  } else {
    orderItems.push(itemData);
  }
  markProductAsAdded(selectedProductCard.querySelector(".agregar-producto"));
  calculateOrderSummary();
  setProductModalOpen(false);
});

productOverlay.addEventListener("click", (event) => {
  if (event.target === productOverlay) {
    setProductModalOpen(false);
  }
});

orderButton.addEventListener("click", () => setOrderPanelOpen(true));
backToMenuButton.addEventListener("click", () => {
  if (ensureNoUnsavedChanges()) setOrderPanelOpen(false);
});
confirmOrderButton.addEventListener("click", () => {
  if (!confirmOrderButton.disabled && ensureNoUnsavedChanges()) {
    showCheckout();
  }
});

backToSummaryButton.addEventListener("click", () => {
  if (backToSummaryButton.disabled) return;
  setConfirmReturnOpen(true);
});

if (cancelReturnButton) {
  cancelReturnButton.addEventListener("click", () => {
    setConfirmReturnOpen(false);
  });
}

if (acceptReturnButton) {
  acceptReturnButton.addEventListener("click", () => {
    setConfirmReturnOpen(false);
    showOrderSummary();
  });
}

if (confirmReturnOverlay) {
  confirmReturnOverlay.addEventListener("click", (event) => {
    if (event.target === confirmReturnOverlay) {
      setConfirmReturnOpen(false);
    }
  });
}

finalizeCheckoutButton.addEventListener("click", () => {
  const paymentMethod = document.querySelector(
    'input[name="metodo-pago"]:checked',
  ).value;
  finalizeCheckoutButton.disabled = true;
  finalizeCheckoutButton.textContent = "Pedido enviado";

  backToSummaryButton.disabled = true;
  const paymentFieldset = document.querySelector(".checkout-payment");
  if (paymentFieldset) paymentFieldset.disabled = true;
  document.querySelectorAll('input[name="metodo-pago"]').forEach((input) => {
    input.disabled = true;
  });
  checkoutView.classList.add("pedido-confirmado");

  showToast(`Pedido enviado a cocina · ${paymentMethod}`, "success");
});

orderOverlay.addEventListener("click", (event) => {
  if (event.target === orderOverlay) {
    setOrderPanelOpen(false);
  }
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    if (
      confirmReturnOverlay &&
      !confirmReturnOverlay.hidden &&
      confirmReturnOverlay.classList.contains("abierto")
    ) {
      setConfirmReturnOpen(false);
      return;
    }
    setOrderPanelOpen(false);
    setProductModalOpen(false);
  }
});

function markProductAsAdded(button) {
  const resetTimer = productResetTimers.get(button);
  clearTimeout(resetTimer);
  button.classList.add("producto-anadido");
  button.querySelector(".material-symbols-outlined").textContent =
    "check_circle";
  button.lastChild.textContent = " Añadido";
  button.setAttribute("aria-pressed", "true");
  productResetTimers.set(
    button,
    setTimeout(() => {
      button.classList.remove("producto-anadido");
      button.querySelector(".material-symbols-outlined").textContent = "add";
      button.lastChild.textContent = " Añadir";
      button.removeAttribute("aria-pressed");
    }, 1000),
  );
}

addProductButtons.forEach((button) => {
  button.addEventListener("click", () => {
    if (finalizeCheckoutButton.disabled) {
      showToast("El pedido ya fue enviado a cocina", "error");
      return;
    }
    const card = button.closest(".producto-card");
    const existingItem = orderItems.find((item) => item.card === card);
    if (existingItem) {
      existingItem.quantity += 1;
    } else {
      orderItems.push({
        card,
        name: card.querySelector("h2").textContent,
        image: card.querySelector("img").src,
        price: parseProductPrice(card),
        quantity: 1,
        removedIngredients: [],
        extraIngredients: [],
      });
    }
    calculateOrderSummary();
    markProductAsAdded(button);
  });
});
