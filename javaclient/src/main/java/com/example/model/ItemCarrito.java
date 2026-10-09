package com.example.model;

/** Un producto con su cantidad, tanto en la factura de Caja como dentro de un pedido. */
public class ItemCarrito {

    private final int productoId; // 0 mientras no haya servidor
    private final String nombre;
    private final int precio;
    private int cantidad;

    public ItemCarrito(int productoId, String nombre, int precio, int cantidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public int getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getSubtotal() {
        return precio * cantidad;
    }
}
