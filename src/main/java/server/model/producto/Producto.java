package server.model.producto;

import server.model.ingrediente.Ingrediente;

import java.io.Serializable;
import java.util.List;

public class Producto implements Serializable, Comparable<Producto>{

    private int id;
    private float precio;
    private String descripcion;
    private String imagenURL;
    private String categoria;
    private String nombre;
    private List<Ingrediente> ingredientes;

    public Producto(int id, float precio, String descripcion, String categoria, String nombre) {
        this.id = id;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.nombre = nombre;
    }

    public boolean addIngrediente(Ingrediente ingrediente) {
        return this.ingredientes.add(ingrediente);
    }

    public boolean removeIngrediente(Ingrediente ingrediente) {
        return this.ingredientes.remove(ingrediente);
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenURL() {
        return imagenURL;
    }

    public void setImagenURL(String imagenURL) {
        this.imagenURL = imagenURL;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public int compareTo(Producto o) {
        return Integer.compare(id, o.getId());
    }
    @Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj == null || getClass()!=obj.getClass()) return false;
        Producto producto=(Producto) obj;
        return (id==producto.id);
    }
}
