package server.model.ingrediente.dao;
import server.database.Database;
import server.model.ingrediente.Ingrediente;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IngredienteDao implements IngredienteDaoInterface {
    @Override
    public Ingrediente insertar(Ingrediente ingrediente) throws SQLException {
        String sql="INSERT INTO ingrediente (id,descripcion,cantidad,nombre) VALUES (?,?,?,?)";
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1,ingrediente.getId());
            stmt.setString(2,ingrediente.getDescripcion());
            stmt.setInt(3,ingrediente.getCantidad());
            stmt.setString(4,ingrediente.getNombre());
            stmt.executeUpdate();
            return ingrediente;
        }
    }

    @Override
    public List<Ingrediente> buscarTodos(int inicio, int finalnum) throws SQLException {
        String sql="SELECT id,descripcion,cantidad,nombre FROM ingrediente ORDER BY id LIMIT ? OFFSET ?";
        List<Ingrediente> ingredientes=new ArrayList<>();
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            int limite=finalnum-inicio+1;
            stmt.setInt(1,limite);
            stmt.setInt(2,inicio);
            try(ResultSet rs=stmt.executeQuery()){
                while(rs.next()){
                    ingredientes.add(mapearIngrediente(rs));
                }
            }
        }
        return ingredientes;
    }

    @Override
    public Ingrediente actualizar(int id, Ingrediente newIngrediente) throws SQLException {
        String sql="UPDATE ingrediente SET id=?, descripcion=?, cantidad=?,nombre=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1,newIngrediente.getId());
            stmt.setString(2,newIngrediente.getDescripcion());
            stmt.setInt(3,newIngrediente.getCantidad());
            stmt.setString(4,newIngrediente.getNombre());
            stmt.setInt(5,id);
            int filasAfectadas=stmt.executeUpdate();
            if(filasAfectadas==0){
                return null;
            }
            return newIngrediente;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql="DELETE FROM ingrediente WHERE id=?";
        try(Connection conn=Database.getConnection();PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1,id);
            int filasAfectadas=stmt.executeUpdate();
            return filasAfectadas>0;
        }
    }

    @Override
    public int contar() throws SQLException {
        String sql="SELECT COUNT(*) FROM ingrediente";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()){
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public Ingrediente getIngredienteByNombre(String nombre) throws SQLException {
        String sql="SELECT id,descripcion,cantidad,nombre FROM ingrediente WHERE nombre=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1,nombre);
            try(ResultSet rs=stmt.executeQuery()){
                if(rs.next()){
                    return mapearIngrediente(rs);
                }
                return null;
            }
        }
    }

    @Override
    public Ingrediente buscarPorId(int id) throws SQLException {
        String sql="SELECT id,descripcion,cantidad,nombre FROM ingrediente WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1,id);
            try(ResultSet rs=stmt.executeQuery()){
                if(rs.next()){
                    return mapearIngrediente(rs);
                }
                return null;
            }
        }
    }

    private Ingrediente mapearIngrediente(ResultSet rs) throws SQLException{
        return new Ingrediente(
                rs.getInt("id"),
                rs.getString("descripcion"),
                rs.getInt("cantidad"),
                rs.getString("nombre")
        );
    }
}