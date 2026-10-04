package com.proyectoApi.api.service;

import com.proyectoApi.api.dto.AdicionalDto;
import com.proyectoApi.api.dto.IngredienteDto;
import com.proyectoApi.api.dto.MesaDto;
import com.proyectoApi.api.dto.ProductoDto;
import com.proyectoApi.api.rmi.RmiGateway;
import server.model.ingrediente.Ingrediente;
import server.model.mesa.Mesa;
import server.model.producto.Producto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Consultas de solo lectura: mesas, menú y adicionales. */
public class MenuService {

    private final RmiGateway rmi;
    private final MenuMetadata metadata;
    private final long cacheMillis;

    private List<ProductoDto> cachedMenu = null;
    private long cachedAt = 0;

    public MenuService(RmiGateway rmi, MenuMetadata metadata, int cacheSeconds) {
        this.rmi = rmi;
        this.metadata = metadata;
        this.cacheMillis = Math.max(0, cacheSeconds) * 1000L;
    }

    public List<MesaDto> tables() {
        List<Mesa> mesas = rmi.mesas(stub -> {
            int total = stub.contar();
            return total == 0 ? List.<Mesa>of() : stub.getMesa(0, total - 1);
        });
        return mesas.stream().map(m -> new MesaDto(m.getId())).sorted(Comparator.comparingInt(MesaDto::id)).toList();
    }

    public synchronized List<ProductoDto> products() {
        long now = System.currentTimeMillis();
        if (cachedMenu != null && cacheMillis > 0 && now - cachedAt < cacheMillis) return cachedMenu;
        List<ProductoDto> menu = loadMenu();
        cachedMenu = menu;
        cachedAt = now;
        return menu;
    }

    public List<AdicionalDto> extras() {
        return metadata.adicionales();
    }

    private List<ProductoDto> loadMenu() {
        // Productos + ingredientes de cada uno, en una sola pasada por el stub (N+1 llamadas RMI, pero un menú es pequeño).
        record Row(Producto producto, List<Ingrediente> ingredientes) {
        }
        List<Row> rows = rmi.productos(stub -> {
            int total = stub.contar();
            if (total == 0) return List.<Row>of();
            List<Row> result = new ArrayList<>();
            for (Producto p : stub.getProductos(0, total - 1)) {
                result.add(new Row(p, stub.getIngredientesPerProduct(p.getId())));
            }
            return result;
        });
        return rows.stream().map(r -> toDto(r.producto(), r.ingredientes()))
                .sorted(Comparator.comparingInt(ProductoDto::id)).toList();
    }

    private ProductoDto toDto(Producto p, List<Ingrediente> ingredientes) {
        var base = metadata.baseIngredients(p.getNombre());
        List<IngredienteDto> ings = ingredientes.stream()
                .map(i -> new IngredienteDto(i.getId(), i.getNombre(), base.contains(Text.key(i.getNombre()))))
                .toList();
        return new ProductoDto(p.getId(), p.getNombre(), p.getDescripcion(), Text.money(p.getPrecio()),
                p.getImagenURL(), p.getCategoria(), ings, metadata.drinkOptions(p.getNombre()));
    }
}
