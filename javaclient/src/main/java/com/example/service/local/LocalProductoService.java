package com.example.service.local;

import java.util.ArrayList;
import java.util.List;

import com.example.service.ProductoService;

import server.model.producto.Producto;

/** Catálogo de prueba (los 15 productos de Caja) mientras no hay servidor. */
public class LocalProductoService implements ProductoService {

    // nombre, precio, imagen (en /images/), categoría
    private static final String[][] CATALOGO = {
        {"Hamburguesa Rústica", "28000", "hamburguesa-rustica", "Hamburguesas"},
        {"Ham. Carbón & Queso", "32000", "hamburguesa-carbon-queso", "Hamburguesas"},
        {"Perro Especial Carbón", "20000", "perro-especial-carbon", "Perros"},
        {"Perro Suizo Ahumado", "23000", "perro-suizo-ahumado", "Perros"},
        {"Salchipapa Clásica", "18000", "salchipapa-clasica", "Salchipapas"},
        {"Salchipapa Sazonada", "25000", "salchipapa-sazonada", "Salchipapas"},
        {"Papa Loca Carbón y Sazón", "34000", "papa-loca-carbon-sazon", "Papas locas"},
        {"Papa Loca BBQ Pork", "31000", "papa-loca-bbq-pork", "Papas locas"},
        {"Desgranado Mixto", "26000", "desgranado-mixto", "Desgranados"},
        {"Desgranado Ranchero", "28000", "desgranado-ranchero", "Desgranados"},
        {"Limonada de Coco", "10000", "limonada-coco", "Bebidas"},
        {"Limonada Cerezada", "9000", "limonada-cerezada", "Bebidas"},
        {"Gaseosa Personal", "5000", "gaseosa-personal", "Bebidas"},
        {"Jugo Natural en Agua", "8000", "jugo-natural-agua", "Bebidas"},
        {"Jugo Natural en Leche", "8000", "jugo-natural-leche", "Bebidas"}
    };

    @Override
    public List<Producto> listar() {
        List<Producto> productos = new ArrayList<>();
        for (int i = 0; i < CATALOGO.length; i++) {
            String[] fila = CATALOGO[i];
            Producto p = new Producto(i + 1, Float.parseFloat(fila[1]), "", fila[3], fila[0]);
            p.setImagenURL("/images/" + fila[2] + ".jpg");
            productos.add(p);
        }
        return productos;
    }
}
