package com.proyectoApi.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Cuerpo de POST /api/pedidos. El frontend envía más campos (precios, ingredientes excluidos, adicionales...);
 * se ignoran porque el servidor recalcula el total con los precios de la BD.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoRequest(Integer idMesa, String metodo, List<Linea> productos) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Linea(Integer idProducto, Integer cantidad) {
    }
}
