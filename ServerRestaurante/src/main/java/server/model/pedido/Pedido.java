package server.model.pedido;

import server.model.empleado.Empleado;
import server.model.mesa.Mesa;
import server.model.producto.Producto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class Pedido implements Serializable, Comparable<Pedido> {

    private int id;
    private String metodoPago;
    private float precioTotal;
    private LocalDateTime fechaPedido;
    private String estado;
    private Mesa mesaAsignada;
    private List<Producto> productos;
    private List<Empleado> encargados;

    public Pedido(int id, LocalDateTime fechaPedido, Mesa mesaAsignada) {
        this.id = id;
        this.fechaPedido = fechaPedido;
        this.mesaAsignada = mesaAsignada;
        this.estado="Pendiente";
        productos=new LinkedList<>();
        encargados=new LinkedList<>();
        precioTotal=0;
        metodoPago="Ninguno";
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public float getPrecioTotal() {
        float total = 0;
        for (Producto producto : productos) {
            total += producto.getPrecio();
        }
        this.precioTotal=total;
        return total;
    }

    public void setPrecioTotal(float precioTotal) {
        this.precioTotal = precioTotal;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Mesa getMesaAsignada() {
        return mesaAsignada;
    }

    public void setMesaAsignada(Mesa mesaAsignada) {
        this.mesaAsignada = mesaAsignada;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Empleado> getEncargados() {
        return encargados;
    }

    @Override
    public int compareTo(Pedido o) {
        return Integer.compare(this.id, o.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj){
            return true;
        }
        if(obj==null || getClass()!=obj.getClass()){
            return false;
        }
        Pedido pedido=(Pedido) obj;
        if(this.id==pedido.getId()){
            return true;
        }
        return false;
    }

    public boolean addProducto(Producto producto) {
        return productos.add(producto);
    }
    public boolean removeProducto(Producto producto) {
        return productos.remove(producto);
    }
    public boolean addEncargado(Empleado empleado) {
        return encargados.add(empleado);
    }

    public boolean removeEmpleado(Empleado empleado) {
        return encargados.remove(empleado);
    }

    @Override
    public String toString() {
        return "Pedido con id: "+id+ "Precio total: "+getPrecioTotal();
    }
}
