const filterOptions = document.querySelectorAll('.filtro-opcion');
const productCards = document.querySelectorAll('.producto-card');
const searchInput = document.querySelector('.buscador-productos input');

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
