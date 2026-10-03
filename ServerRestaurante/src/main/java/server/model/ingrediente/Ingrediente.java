package server.model.ingrediente;

import java.io.Serializable;

public class Ingrediente implements Serializable, Comparable<Ingrediente>{

    private int id;
    private String descripcion;
    private int cantidad;
    private String nombre;

    public Ingrediente(int id, String descripcion, int cantidad, String nombre) {
        this.id = id;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.nombre = nombre;
    }


    @Override
    public int compareTo(Ingrediente o) {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Ingrediente that = (Ingrediente) obj;
        return id == that.id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Ingrediente: "+nombre+" Con id: "+id+" Con cantidad: "+cantidad;
    }
}
