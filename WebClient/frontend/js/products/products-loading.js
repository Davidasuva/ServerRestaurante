import { setLoading, waitForImages } from "../shared/loading.js";

function addProductSkeleton(card) {
  if (card.querySelector(".producto-skeleton")) {
    return;
  }

  const skeleton = document.createElement("div");
  skeleton.className = "producto-skeleton";
  skeleton.setAttribute("aria-hidden", "true");
  skeleton.innerHTML = `
    <span class="producto-skeleton-image skeleton"></span>
    <span class="producto-skeleton-title skeleton"></span>
    <span class="producto-skeleton-price skeleton"></span>
    <span class="producto-skeleton-button skeleton"></span>
  `;
  card.append(skeleton);
}

export function initializeProductLoading() {
  const images = [...document.querySelectorAll(".producto-card img")];
  const productView = document.querySelector(".productos");
  document.querySelectorAll(".producto-card").forEach(addProductSkeleton);
  waitForImages(images, () => setLoading(productView, false, "productos-cargando"));
}
