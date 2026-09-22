package server.model.empleado;

import java.io.Serializable;

public class Empleado implements Serializable, Comparable<Empleado>{

    private int cedula;
    private String cargo;
    private String nombre;
    private String contrasena;

    public Empleado(int cedula, String cargo, String nombre, String contrasena) {
        this.cedula = cedula;
        this.cargo = cargo;
        this.nombre = nombre;
        this.contrasena = contrasena;
    }

    public int getCedula() {
        return cedula;
    }

    public void setCedula(int cedula) {
        this.cedula = cedula;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contraseña) {
        this.contrasena = contraseña;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Empleado empleado = (Empleado) obj;
        return cedula == empleado.cedula;
    }
    @Override
    public int compareTo(Empleado o) {
        return Integer.compare(this.cedula, o.cedula);
    }
}
