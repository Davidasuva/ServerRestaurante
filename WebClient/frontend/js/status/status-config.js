export const ORDER_STATUS_CONFIG = {
    EN_COLA: {
        icon: "hourglass_top",
        title: "¡Tu pedido está<br />en cola!",
        isReady: false,
        requiresQueueCheck: false,
        note: (ordersInPreparation, queuePosition) =>
            `Hay ${ordersInPreparation} pedido${ordersInPreparation === 1 ? "" : "s"} en preparación. Posición en cola: ${queuePosition}.`,
    },
    EN_PREPARACION: {
        icon: "skillet",
        title: "¡Tu pedido está en<br />preparación!",
        isReady: false,
        requiresQueueCheck: true,
        note: () => "La cocina está preparando tu pedido.",
    },
    LISTO: {
        icon: "check_circle",
        title: "¡Tu pedido está listo!",
        isReady: true,
        requiresQueueCheck: false,
        note: () => "Puedes recogerlo o disfrutarlo en tu mesa.",
    },
};

export const ORDER_STATUS_VALUES = Object.freeze(Object.keys(ORDER_STATUS_CONFIG));

export function getOrderStatusConfig(state) {
    return ORDER_STATUS_CONFIG[state] || ORDER_STATUS_CONFIG.EN_COLA;
}

export function getInitialOrderStatus(state, ordersInPreparation) {
    const config = getOrderStatusConfig(state);
    return config.requiresQueueCheck && ordersInPreparation > 0 ? "EN_COLA" : state;
}
