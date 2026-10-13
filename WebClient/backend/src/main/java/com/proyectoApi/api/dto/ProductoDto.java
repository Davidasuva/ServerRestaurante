package com.proyectoApi.api.dto;

import java.util.List;

public record ProductoDto(
        int id,
        String nombre,
        String descripcion,
        double precio,
        String imagen,
        String categoria,
        List<IngredienteDto> ingredientes,
        List<String> opcionesBebida) {
}
