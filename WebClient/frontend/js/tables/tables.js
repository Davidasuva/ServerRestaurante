import { setLoading } from "../shared/loading.js";

const tableView = document.querySelector(".inicio");

function finishTableLoading() {
    requestAnimationFrame(() => {
        setLoading(tableView, false);
    });
}

window.addEventListener("load", finishTableLoading, { once: true });
