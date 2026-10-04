export function lockProductNavigation() {
  const productUrl = window.location.href;
  window.history.replaceState({ productPage: true }, "", productUrl);
  window.history.pushState({ productPageGuard: true }, "", productUrl);
  window.addEventListener("popstate", () => {
    window.history.pushState({ productPageGuard: true }, "", productUrl);
  });
}
