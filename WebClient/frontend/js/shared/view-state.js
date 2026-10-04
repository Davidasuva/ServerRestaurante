export function setViewState(element, state, states = ["loading", "empty", "error", "success"]) {
  if (!element) return;

  states.forEach((stateName) => element.classList.remove(`estado-${stateName}`));
  if (state) element.classList.add(`estado-${state}`);
}
