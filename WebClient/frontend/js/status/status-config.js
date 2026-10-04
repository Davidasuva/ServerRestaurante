export const ORDER_STATUS_CONFIG = {
    PENDIENTE: {
        icon: "receipt_long",
        title: "¡Tu pedido fue recibido!",
        isReady: false,
        isCompleted: false,
        isError: false,
        visualState: "en-cola",
        note: () => "Estamos registrando tu pedido.",
    },
    EN_COLA: {
        icon: "hourglass_top",
        title: "¡Tu pedido está<br />en cola!",
        isReady: false,
        isCompleted: false,
        requiresQueueCheck: false,
        isError: false,
        visualState: "en-cola",
        note: (ordersInPreparation, queuePosition) =>
            ordersInPreparation === null || queuePosition === null
                ? "La información de la cola aún no está disponible."
                : `Hay ${ordersInPreparation} pedido${ordersInPreparation === 1 ? "" : "s"} en preparación. Posición en cola: ${queuePosition}.`,
    },
    PREPARANDOSE: {
        icon: "skillet",
        title: "¡Tu pedido está en<br />preparación!",
        isReady: false,
        isCompleted: false,
        isError: false,
        visualState: "preparandose",
        note: () => "La cocina está preparando tu pedido.",
    },
    PREPARADO: {
        icon: "check_circle",
        title: "¡Tu pedido está listo!",
        isReady: true,
        isCompleted: true,
        isError: false,
        visualState: "preparado",
        requiresQueueCheck: false,
        note: () => "Puedes recogerlo o disfrutarlo en tu mesa.",
    },
    ENTREGADO: {
        icon: "check_circle",
        title: "¡Tu pedido fue entregado!",
        isReady: false,
        isCompleted: true,
        isError: false,
        visualState: "entregado",
        requiresQueueCheck: false,
        note: () => "El pedido fue entregado correctamente.",
    },
    CANCELADO: {
        icon: "close",
        title: "Pedido cancelado",
        isReady: false,
        isCompleted: false,
        isError: true,
        visualState: "cancelado",
        requiresQueueCheck: false,
        note: () => "Este pedido fue cancelado.",
    },
};

export const ORDER_STATUS_VALUES = Object.freeze(Object.keys(ORDER_STATUS_CONFIG));

export function getOrderStatusConfig(state) {
    return ORDER_STATUS_CONFIG[state] || ORDER_STATUS_CONFIG.EN_COLA;
}
