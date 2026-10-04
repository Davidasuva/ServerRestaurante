package com.proyectoApi.api.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoApi.api.dto.AdicionalDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Datos del menú que el esquema de la BD no guarda (ingredientes base, opciones de bebida y adicionales con precio).
 * Se leen de menu-config.json y se asocian a los productos por NOMBRE (sin tildes ni mayúsculas).
 */
public class MenuMetadata {

    private static final Logger LOG = Logger.getLogger(MenuMetadata.class.getName());

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductoMeta(List<String> ingredientesBase, List<String> opcionesBebida) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Data(List<AdicionalDto> adicionales, Map<String, ProductoMeta> productos) {
    }

    private final List<AdicionalDto> adicionales;
    private final Map<String, ProductoMeta> porProducto = new HashMap<>();

    public MenuMetadata(List<AdicionalDto> adicionales, Map<String, ProductoMeta> productos) {
        this.adicionales = adicionales == null ? List.of() : List.copyOf(adicionales);
        if (productos != null) productos.forEach((name, meta) -> porProducto.put(Text.key(name), meta));
    }

    public static MenuMetadata empty() {
        return new MenuMetadata(List.of(), Map.of());
    }

    public static MenuMetadata load(InputStream in, ObjectMapper mapper) throws IOException {
        Data data = mapper.readValue(in, Data.class);
        return new MenuMetadata(data.adicionales(), data.productos());
    }

    public static MenuMetadata loadOrEmpty(InputStream in, ObjectMapper mapper, String origin) {
        if (in == null) {
            LOG.warning("No se encontró " + origin + ": sin ingredientes base, opciones de bebida ni adicionales.");
            return empty();
        }
        try (in) {
            return load(in, mapper);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "menu-config inválido (" + origin + "), se ignora", e);
            return empty();
        }
    }

    public List<AdicionalDto> adicionales() {
        return adicionales;
    }

    public Set<String> baseIngredients(String productName) {
        ProductoMeta meta = porProducto.get(Text.key(productName));
        Set<String> result = new HashSet<>();
        if (meta != null && meta.ingredientesBase() != null) meta.ingredientesBase().forEach(n -> result.add(Text.key(n)));
        return result;
    }

    public List<String> drinkOptions(String productName) {
        ProductoMeta meta = porProducto.get(Text.key(productName));
        return meta == null || meta.opcionesBebida() == null ? List.of() : meta.opcionesBebida();
    }
}
