package com.example.service;

import com.example.service.local.LocalMesaService;
import com.example.service.local.LocalPedidoService;
import com.example.service.local.LocalProductoService;

/**
 * Punto único de acceso a los services. Los controladores piden aquí lo que necesitan;
 * para cambiar de datos locales a servidor se cambian estas tres líneas.
 */
public final class Servicios {

    private static final PedidoService PEDIDOS = new LocalPedidoService();
    private static final ProductoService PRODUCTOS = new LocalProductoService();
    private static final MesaService MESAS = new LocalMesaService();

    private Servicios() {}

    public static PedidoService pedidos() {
        return PEDIDOS;
    }

    public static ProductoService productos() {
        return PRODUCTOS;
    }

    public static MesaService mesas() {
        return MESAS;
    }
}
