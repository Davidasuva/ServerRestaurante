package server.model.producto.dao;

import server.database.Database;
import server.model.ingrediente.Ingrediente;
import server.model.producto.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ProductoDao implements ProductoDaoInterface{


    @Override
    public Producto insertar(Producto producto) throws SQLException {
        String sql="INSERT INTO producto(id,precio,descripcion,imagen,categoria,nombre) VALUES (?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1,producto.getId());
            stmt.setFloat(2,producto.getPrecio());
            stmt.setString(3,producto.getDescripcion());
            stmt.setString(4,producto.getImagenURL());
            stmt.setString(5,producto.getCategoria());
            stmt.setString(6,producto.getNombre());
            stmt.executeUpdate();
            return producto;
        }
    }

    @Override
    public Producto buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, precio, descripcion, imagen, categoria, nombre FROM producto WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()){
                    return mapearProducto(rs);
                }
                return null;
            }
        }
    }

    @Override
    public Producto buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id, precio, descripcion, imagen, categoria, nombre FROM producto WHERE nombre = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()){
                    return mapearProducto(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Producto> buscarPorCategoria(String categoria) throws SQLException {
        String sql = "SELECT id, precio, descripcion, imagen, categoria, nombre FROM producto WHERE categoria = ?";
        List<Producto> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearProducto(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public List<Producto> buscarTodos(int inicio, int finalnum) throws SQLException {
        String sql = "SELECT id, precio, descripcion, imagen, categoria, nombre FROM producto ORDER BY id LIMIT ? OFFSET ?";
        List<Producto> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int limite = finalnum - inicio + 1;
            stmt.setInt(1, limite);
            stmt.setInt(2, inicio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearProducto(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public Producto actualizar(int id, Producto newProducto) throws SQLException {
        String sql = "UPDATE producto SET id = ?, precio = ?, descripcion = ?, imagen = ?, categoria = ?, nombre = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newProducto.getId());
            stmt.setFloat(2, newProducto.getPrecio());
            stmt.setString(3, newProducto.getDescripcion());
            stmt.setString(4, newProducto.getImagenURL());
            stmt.setString(5, newProducto.getCategoria());
            stmt.setString(6, newProducto.getNombre());
            stmt.setInt(7, id);
            int filas = stmt.executeUpdate();
            if(filas==0){
                return null;
            }
            return newProducto;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public List<Ingrediente> buscarIngredientes(int productoId) throws SQLException {
        String sql = "SELECT i.id, i.descripcion, i.cantidad, i.nombre " +
                "FROM ingrediente i " +
                "JOIN ingredienteProducto pi ON pi.id_ingrediente = i.id " +
                "WHERE pi.id_producto = ?";
        List<Ingrediente> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Ingrediente(
                            rs.getInt("id"),
                            rs.getString("descripcion"),
                            rs.getInt("cantidad"),
                            rs.getString("nombre")
                    ));
                }
            }
        }
        return resultado;
    }

    @Override
    public boolean agregarIngrediente(int productoId, int ingredienteId) throws SQLException {
        String sql = "INSERT INTO ingredienteProducto (id_producto , id_ingrediente) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            stmt.setInt(2, ingredienteId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean quitarIngrediente(int productoId, int ingredienteId) throws SQLException {
        String sql = "DELETE FROM ingredienteProducto WHERE id_producto  = ? AND id_ingrediente = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            stmt.setInt(2, ingredienteId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException{
        Producto producto = new Producto(
                rs.getInt("id"),
                rs.getFloat("precio"),
                rs.getString("descripcion"),
                rs.getString("categoria"),
                rs.getString("nombre")
        );
        producto.setImagenURL(rs.getString("imagen"));
        return producto;
    }
}
