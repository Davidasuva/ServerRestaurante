package com.proyectoApi.api.dto;

/** `base` = ingrediente que no se puede quitar del producto (viene de menu-config.json, no de la BD). */
public record IngredienteDto(int id, String nombre, boolean base) {
}
