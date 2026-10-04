package com.proyectoApi.api.dto;

public record EstadoPedidoDto(int id, String estado, int mesa, int pedidosEnPreparacion, int posicionCola) {
}
