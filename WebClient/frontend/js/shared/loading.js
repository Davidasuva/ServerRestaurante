export function setLoading(element, isLoading, loadingClass = "cargando") {
  if (!element) return;

  element.classList.toggle(loadingClass, isLoading);
  element.setAttribute("aria-busy", String(isLoading));
}

export function waitForImages(images, onComplete) {
  if (!images.length) {
    onComplete();
    return;
  }

  let completedImages = 0;
  const markImageAsCompleted = () => {
    completedImages += 1;
    if (completedImages === images.length) onComplete();
  };

  images.forEach((image) => {
    if (image.complete) markImageAsCompleted();
    else {
      image.addEventListener("load", markImageAsCompleted, { once: true });
      image.addEventListener("error", markImageAsCompleted, { once: true });
    }
  });
}
