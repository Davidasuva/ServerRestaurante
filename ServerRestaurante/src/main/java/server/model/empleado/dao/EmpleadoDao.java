package server.model.empleado.dao;
import server.database.Database;
import server.model.empleado.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDao implements EmpleadoDaoInterface {

    @Override
    public Empleado insertar(Empleado empleado) throws SQLException {
        String sql= "INSERT INTO empleado (cedula,cargo,nombre,contraseña) VALUES (?,?,?,?)";
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1, empleado.getCedula());
            stmt.setString(2, empleado.getCargo());
            stmt.setString(3, empleado.getNombre());
            stmt.setString(4, empleado.getContrasena());
            stmt.executeUpdate();
            return empleado;
        }
    }

    @Override
    public List<Empleado> buscarTodos(int inicio, int finalnum) throws SQLException {
        String sql = "Select cedula,cargo,nombre,contrasena FROM empleado ORDER BY cedula LIMIT ? OFFSET ?";
        List<Empleado> empleados=new ArrayList<>();
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            int limite=finalnum-inicio+1;
            stmt.setInt(1,limite);
            stmt.setInt(2,inicio);
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    empleados.add(mapearEmpleado(rs));
                }
            }
        }
        return empleados;
    }

    @Override
    public Empleado actualizar(int cedula, Empleado newEmpleado) throws SQLException {
        String sql = "UPDATE empleado SET cedula=?,cargo=?,nombre=?,contrasena=? WHERE cedula=?";
        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newEmpleado.getCedula());
            stmt.setString(2, newEmpleado.getCargo());
            stmt.setString(3, newEmpleado.getNombre());
            stmt.setString(4, newEmpleado.getContrasena());
            stmt.setInt(5, cedula);
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                return null;
            }
            return newEmpleado;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql="DELETE FROM empleado WHERE id=?";
        try(Connection conn=Database.getConnection();PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1,id);
            int filasAfectadas=stmt.executeUpdate();
            return filasAfectadas>0;
        }
    }

    @Override
    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleado";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public Empleado buscarPorCedula(int cedula) throws SQLException {
        String sql="SELECT cedula, cargo, nombre, contrasena FROM empleado WHERE cedula=?";
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1,cedula);
            try(ResultSet rs=stmt.executeQuery()){
                if(rs.next()){
                    return mapearEmpleado(rs);
                }
                return null;
            }

        }
    }

    @Override
    public List<Empleado> buscarPorCargo(String cargo) throws SQLException {
        String sql="SELECT cedula, cargo, nombre, contrasena FROM empleado WHERE cargo=?";
        List<Empleado> resultado=new ArrayList<>();
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setString(1,cargo);
            try(ResultSet rs=stmt.executeQuery()){
                while(rs.next()){
                    resultado.add(mapearEmpleado(rs));
                }
                return null;
            }
        }
    }

    @Override
    public List<Empleado> buscarPorNombre(String nombre) throws SQLException {
        String sql="SELECT cedula, cargo, nombre, contrasena FROM empleado WHERE cargo=?";
        List<Empleado> resultado=new ArrayList<>();
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setString(1,nombre);
            try(ResultSet rs=stmt.executeQuery()){
                while(rs.next()){
                    resultado.add(mapearEmpleado(rs));
                }
                return null;
            }
        }
    }

    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getInt("cedula"),
                rs.getString("cargo"),
                rs.getString("nombre"),
                rs.getString("contrasena")
        );
    }
}
