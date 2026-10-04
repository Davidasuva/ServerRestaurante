import { tableElements } from "./table-elements.js";

export const tableNumber = new URLSearchParams(window.location.search).get("mesa");

if (tableElements.tableLabel && /^\d+$/.test(tableNumber || "")) {
  tableElements.tableLabel.textContent = `M. ${tableNumber}`;
}
