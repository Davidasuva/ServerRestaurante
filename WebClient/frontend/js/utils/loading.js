/**
 * Estado de carga compartido por las vistas (portado del frontend con pantallas de carga: shared/loading.js).
 * Mientras un elemento tiene su clase de carga, el CSS muestra el esqueleto en lugar del contenido.
 */

const DEMO_LOADING_PARAM = "demoLoading";
const DEMO_LOADING_DURATION = 5000;

/** Activa/desactiva la clase de carga y marca el elemento como ocupado para lectores de pantalla. */
export function setLoading(element, isLoading, loadingClass = "cargando") {
  if (!element) return;

  element.classList.toggle(loadingClass, isLoading);
  element.setAttribute("aria-busy", String(isLoading));
}

/**
 * Llama a `onComplete` cuando todas las imágenes terminaron de cargar (o fallaron).
 * `maxWaitMs` evita que el esqueleto se quede para siempre si alguna imagen (p. ej. una `loading="lazy"`
 * fuera de pantalla) tarda en empezar a cargarse.
 */
export function waitForImages(images, onComplete, maxWaitMs = 4000) {
  if (!images.length) {
    onComplete();
    return;
  }

  let finished = false;
  let completedImages = 0;
  const finish = () => {
    if (finished) return;
    finished = true;
    clearTimeout(timer);
    onComplete();
  };
  const timer = setTimeout(finish, maxWaitMs);
  const markImageAsCompleted = () => {
    completedImages += 1;
    if (completedImages === images.length) finish();
  };

  images.forEach((image) => {
    if (image.complete) markImageAsCompleted();
    else {
      image.addEventListener("load", markImageAsCompleted, { once: true });
      image.addEventListener("error", markImageAsCompleted, { once: true });
    }
  });
}

/** `?demoLoading=true` en la URL alarga 5 s las pantallas de carga, para verlas sin necesidad de un servidor lento. */
export function demoLoadingDelay() {
  return new URLSearchParams(window.location.search).get(DEMO_LOADING_PARAM) === "true" ? DEMO_LOADING_DURATION : 0;
}
