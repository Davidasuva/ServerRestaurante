/**
 * Estado visual de una vista (portado de shared/view-state.js): deja en el elemento una sola clase
 * `estado-loading | estado-empty | estado-error | estado-success`. `null` quita todas.
 */
export function setViewState(element, state, states = ["loading", "empty", "error", "success"]) {
  if (!element) return;

  states.forEach((stateName) => element.classList.remove(`estado-${stateName}`));
  if (state) element.classList.add(`estado-${state}`);
}
