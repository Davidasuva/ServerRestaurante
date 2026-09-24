const filterOptions = document.querySelectorAll('.filtro-opcion');
const productCards = document.querySelectorAll('.producto-card');
const searchInput = document.querySelector('.buscador-productos input');
const addProductButtons = document.querySelectorAll('.agregar-producto');
const orderButton = document.querySelector('.pedido');
const orderOverlay = document.querySelector('.pedido-overlay');
const closeOrderButton = document.querySelector('.cerrar-pedido');
const backToMenuButton = document.querySelector('.volver-menu');
const productOverlay = document.querySelector('.producto-overlay');
const productModal = document.querySelector('.producto-modal');
const closeProductButton = document.querySelector('.cerrar-producto');
const confirmProductButton = document.querySelector('.confirmar-producto');
const modalImage = document.querySelector('.producto-modal-imagen');
const modalTitle = document.querySelector('#titulo-producto-modal');
const modalPrice = document.querySelector('.producto-modal-precio');
const modalDescription = document.querySelector('.producto-modal-descripcion');
const orderList = document.querySelector('.lista-pedido');
const emptyOrderState = document.querySelector('.pedido-vacio');
const orderTotalElement = document.querySelector('.total-panel strong');
const confirmOrderButton = document.querySelector('.confirmar-pedido');

let selectedProductCard;
let productQuantity = 1;
let orderQuantity = 0;
let orderTotal = 0;
let orderItems = [];
let selectedOrderItem;
let selectedIngredients = [];
let extraIngredients = [];
let expandedOrderIndex = null;

const availableExtras = [
    { name: 'Porción de Papa a la Francesa', price: 8000 },
    { name: 'Porción de Papa Criolla', price: 8000 },
    { name: 'Extra de Queso Gratinado', price: 5000 },
    { name: 'Extra de Tocineta', price: 6000 },
    { name: 'Extra de Carne o Pollo', price: 9000 }
];

function setOrderPanelOpen(isOpen) {
    orderOverlay.classList.toggle('abierto', isOpen);
    orderOverlay.setAttribute('aria-hidden', String(!isOpen));
    orderButton.setAttribute('aria-expanded', String(isOpen));
    document.body.classList.toggle('pedido-abierto', isOpen);
}

function setProductModalOpen(isOpen) {
    productOverlay.classList.toggle('abierto', isOpen);
    productOverlay.setAttribute('aria-hidden', String(!isOpen));
    document.body.classList.toggle('producto-abierto', isOpen);
}

function parseProductPrice(card) {
    return Number(card.querySelector('.producto-precio').textContent.replace(/[^0-9]/g, ''));
}

function updateOrderSummary() {
    orderButton.classList.toggle('pedido-con-productos', orderItems.length > 0);
    document.querySelector('.cantidad-pedido').textContent = orderQuantity;
    document.querySelector('.total-pedido').textContent = `$${orderTotal.toLocaleString('es-CO')}`;
    orderTotalElement.textContent = `$${orderTotal.toLocaleString('es-CO')}`;
    emptyOrderState.hidden = orderItems.length > 0;
    orderList.hidden = orderItems.length === 0;
    confirmOrderButton.disabled = orderItems.length === 0;
}

function renderOrderItems(animateEditor = false) {
    orderList.innerHTML = orderItems.map((item, index) => `
        <div class="pedido-item ${expandedOrderIndex === index ? 'expandido' : ''}" data-index="${index}">
            <button class="pedido-item-resumen" type="button" aria-expanded="${expandedOrderIndex === index}">
            <img src="${item.image}" alt="${item.name}">
            <span class="pedido-item-info">
                <strong>${item.name}</strong>
                <span>$${item.price.toLocaleString('es-CO')}</span>
            </span>
            <span class="pedido-item-cantidad">x${item.quantity}</span>
            </button>
            ${expandedOrderIndex === index ? renderOrderEditor(item, index, animateEditor) : ''}
        </div>
    `).join('');
    orderList.querySelectorAll('.pedido-item-resumen').forEach((itemButton) => {
        itemButton.addEventListener('click', () => {
            const index = Number(itemButton.closest('.pedido-item').dataset.index);
            expandedOrderIndex = expandedOrderIndex === index ? null : index;
            renderOrderItems(expandedOrderIndex === index);
        });
    });
    bindOrderEditor();
}

function renderOrderEditor(item, index, animateEditor) {
    const ingredients = item.card.dataset.ingredientes.split(',').map((ingredient) => ingredient.trim());
    return `
        <div class="pedido-editor ${animateEditor ? 'animar' : ''}" data-editor-index="${index}">
            <h3>Ingredientes</h3>
            <div class="ingredientes-lista">
                ${ingredients.map((ingredient) => `
                    <label class="ingrediente-fila">
                        <input type="radio" data-ingrediente="${ingredient}" ${item.removedIngredients.includes(ingredient) ? '' : 'checked'}>
                        <span>${ingredient}</span>
                    </label>
                `).join('')}
            </div>
            <h3>Adicionales</h3>
            <div class="adicionales-lista">
                ${availableExtras.map((extra) => `
                    <div class="adicional-fila ${getExtraQuantity(item, extra.name) > 0 ? 'seleccionado' : ''}" data-extra="${extra.name}">
                        <span class="adicional-info"><strong>${extra.name}</strong><small>+$${extra.price.toLocaleString('es-CO')}</small></span>
                        <span class="selector-adicional" aria-label="Cantidad de ${extra.name}">
                            <button class="cambiar-adicional disminuir-adicional" type="button" aria-label="Disminuir ${extra.name}" ${getExtraQuantity(item, extra.name) === 0 ? 'disabled' : ''}>−</button>
                            <strong class="cantidad-adicional">${getExtraQuantity(item, extra.name)}</strong>
                            <button class="cambiar-adicional aumentar-adicional" type="button" aria-label="Aumentar ${extra.name}">+</button>
                        </span>
                    </div>
                `).join('')}
            </div>
            <div class="pedido-editor-footer">
                <button class="guardar-edicion" type="button">Guardar cambios</button>
            </div>
        </div>
    `;
}

function getExtraQuantity(item, extraName) {
    const extra = item.extraIngredients.find((ingredient) => {
        return typeof ingredient === 'string' ? ingredient === extraName : ingredient.name === extraName;
    });
    return typeof extra === 'string' ? 1 : (extra?.quantity || 0);
}

function setExtraQuantity(item, extraName, quantity) {
    const extraIndex = item.extraIngredients.findIndex((ingredient) => {
        return typeof ingredient === 'string' ? ingredient === extraName : ingredient.name === extraName;
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

function bindOrderEditor() {
    const editor = orderList.querySelector('.pedido-editor');
    if (!editor) return;
    const index = Number(editor.dataset.editorIndex);
    const item = orderItems[index];
    editor.querySelectorAll('.ingrediente-fila').forEach((row) => {
        const input = row.querySelector('input');
        row.addEventListener('click', (event) => {
            event.preventDefault();
            input.checked = !input.checked;
        });
    });
    editor.querySelectorAll('.adicional-fila').forEach((row) => {
        const extra = row.dataset.extra;
        row.querySelector('.disminuir-adicional').addEventListener('click', () => {
            setExtraQuantity(item, extra, Math.max(0, getExtraQuantity(item, extra) - 1));
            calculateOrderSummary();
        });
        row.querySelector('.aumentar-adicional').addEventListener('click', () => {
            setExtraQuantity(item, extra, getExtraQuantity(item, extra) + 1);
            calculateOrderSummary();
        });
    });
    editor.querySelector('.guardar-edicion').addEventListener('click', () => {
        item.removedIngredients = [...editor.querySelectorAll('.ingrediente-fila input:not(:checked)')]
            .map((input) => input.dataset.ingrediente);
        editor.querySelector('.guardar-edicion').disabled = true;
        editor.classList.add('cerrando');
        setTimeout(() => {
            expandedOrderIndex = null;
            calculateOrderSummary();
        }, 380);
    });
}

function calculateOrderSummary() {
    orderQuantity = orderItems.reduce((total, item) => total + item.quantity, 0);
    orderTotal = orderItems.reduce((total, item) => {
        const extrasTotal = item.extraIngredients.reduce((extras, ingredient) => {
            const extraName = typeof ingredient === 'string' ? ingredient : ingredient.name;
            const extraQuantity = typeof ingredient === 'string' ? 1 : ingredient.quantity;
            return extras + ((availableExtras.find((extra) => extra.name === extraName)?.price || 0) * extraQuantity);
        }, 0);
        return total + (item.price + extrasTotal) * item.quantity;
    }, 0);
    renderOrderItems();
    updateOrderSummary();
}

function openProductModal(card, orderItem = null) {
    selectedProductCard = card;
    selectedOrderItem = orderItem;
    productQuantity = orderItem ? orderItem.quantity : 1;
    selectedIngredients = card.dataset.ingredientes.split(',').map((ingredient) => ingredient.trim());
    extraIngredients = orderItem ? [...orderItem.extraIngredients] : [];
    modalImage.src = card.querySelector('img').src;
    modalImage.alt = card.querySelector('img').alt;
    modalTitle.textContent = card.querySelector('h2').textContent;
    modalPrice.textContent = card.querySelector('.producto-precio').textContent;
    modalDescription.textContent = card.dataset.descripcion;
    confirmProductButton.textContent = 'Añadir al pedido';
    setProductModalOpen(true);
}

function updateProducts() {
    const selectedCategory = document.querySelector('.filtro-opcion.activo').dataset.categoria;
    const searchText = searchInput.value.trim().toLowerCase();

    productCards.forEach((card) => {
        const matchesCategory = selectedCategory === 'todas' || card.dataset.categoria === selectedCategory;
        const matchesSearch = card.textContent.toLowerCase().includes(searchText);
        card.classList.toggle('oculto', !matchesCategory || !matchesSearch);
    });
}

filterOptions.forEach((option) => {
    option.addEventListener('click', () => {
        filterOptions.forEach((item) => item.classList.remove('activo'));
        option.classList.add('activo');
        updateProducts();
    });
});

searchInput.addEventListener('input', updateProducts);

productCards.forEach((card) => {
    card.addEventListener('click', (event) => {
        if (!event.target.closest('.agregar-producto')) {
            openProductModal(card);
        }
    });
});

closeProductButton.addEventListener('click', () => setProductModalOpen(false));

confirmProductButton.addEventListener('click', () => {
    const itemData = {
        card: selectedProductCard,
        name: selectedProductCard.querySelector('h2').textContent,
        image: selectedProductCard.querySelector('img').src,
        price: parseProductPrice(selectedProductCard),
        quantity: productQuantity,
        removedIngredients: [],
        extraIngredients: []
    };
    if (selectedOrderItem) {
        Object.assign(selectedOrderItem, itemData);
    } else {
        orderItems.push(itemData);
    }
    selectedProductCard.querySelector('.agregar-producto').classList.add('producto-anadido');
    calculateOrderSummary();
    setProductModalOpen(false);
});

productOverlay.addEventListener('click', (event) => {
    if (event.target === productOverlay) {
        setProductModalOpen(false);
    }
});

orderButton.addEventListener('click', () => setOrderPanelOpen(true));
closeOrderButton.addEventListener('click', () => setOrderPanelOpen(false));
backToMenuButton.addEventListener('click', () => setOrderPanelOpen(false));

orderOverlay.addEventListener('click', (event) => {
    if (event.target === orderOverlay) {
        setOrderPanelOpen(false);
    }
});

document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
        setOrderPanelOpen(false);
        setProductModalOpen(false);
    }
});

addProductButtons.forEach((button) => {
    let resetTimer;

    button.addEventListener('click', () => {
        const card = button.closest('.producto-card');
        const existingItem = orderItems.find((item) => item.card === card);
        if (existingItem) {
            existingItem.quantity += 1;
        } else {
            orderItems.push({
                card,
                name: card.querySelector('h2').textContent,
                image: card.querySelector('img').src,
                price: parseProductPrice(card),
                quantity: 1,
                removedIngredients: [],
                extraIngredients: []
            });
        }
        calculateOrderSummary();
        const icon = button.querySelector('.material-symbols-outlined');

        clearTimeout(resetTimer);
        button.classList.add('producto-anadido');
        icon.textContent = 'check_circle';
        button.lastChild.textContent = ' Añadido';
        button.setAttribute('aria-pressed', 'true');

        resetTimer = setTimeout(() => {
            button.classList.remove('producto-anadido');
            icon.textContent = 'add';
            button.lastChild.textContent = ' Añadir';
            button.removeAttribute('aria-pressed');
        }, 1000);
    });
});
