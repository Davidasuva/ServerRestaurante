package server.model.pedido.dao;

import server.database.Database;
import server.model.empleado.Empleado;
import server.model.mesa.Mesa;
import server.model.pedido.Pedido;
import server.model.producto.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoDao implements PedidoDaoInterface {

    @Override
    public Pedido insertar(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedido (id, metodo, precio_total,fecha_pedido, estado, id_mesa) VALUES (?,? ,?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedido.getId());
            stmt.setString(2, pedido.getMetodoPago());
            stmt.setFloat(3, pedido.getPrecioTotal());
            stmt.setTimestamp(4, Timestamp.valueOf(pedido.getFechaPedido()));
            stmt.setString(5, pedido.getEstado());
            stmt.setInt(6, pedido.getMesaAsignada().getId());
            stmt.executeUpdate();
            return pedido;
        }
    }

    @Override
    public Pedido buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, metodo, precio_total,fecha_pedido, estado, id_mesa FROM pedido WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()){
                    return mapearPedido(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Pedido> buscarPorFecha(LocalDateTime fecha1, LocalDateTime fecha2) throws SQLException {
        String sql = "SELECT id, metodo, precio_total,fecha_pedido, estado, id_mesa FROM pedido WHERE fecha_pedido BETWEEN ? AND ?";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(fecha1));
            stmt.setTimestamp(2, Timestamp.valueOf(fecha2));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearPedido(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public List<Pedido> buscarPorMesa(int mesaId) throws SQLException {
        String sql = "SELECT id, metodo, precio_total,fecha_pedido, estado, id_mesa FROM pedido WHERE id_mesa = ?";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mesaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearPedido(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public List<Pedido> buscarPorEstado(String estado) throws SQLException {
        String sql = "SELECT id, metodo, precio_total,fecha_pedido, estado, id_mesa FROM pedido WHERE estado = ?";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearPedido(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public List<Pedido> buscarPorRangoPrecio(float inicio, float finalnum) throws SQLException {
        String sql = "SELECT id, metodo, precio_total,fecha_pedido, estado, id_mesa FROM pedido WHERE precio_total BETWEEN ? AND ?";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setFloat(1, inicio);
            stmt.setFloat(2, finalnum);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearPedido(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public List<Pedido> buscarTodos(int inicio, int finalnum) throws SQLException {
        String sql = "SELECT id, metodo, precio_total,fecha_pedido, estado, id_mesa FROM pedido ORDER BY id LIMIT ? OFFSET ?";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int limite = finalnum - inicio + 1;
            stmt.setInt(1, limite);
            stmt.setInt(2, inicio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearPedido(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public Pedido actualizar(int id, Pedido nuevoPedido) throws SQLException {
        String sql = "UPDATE pedido SET id = ?, metodo = ?, precio_total=?,fecha_pedido = ?, estado = ?, id_mesa = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuevoPedido.getId());
            stmt.setString(2, nuevoPedido.getMetodoPago());
            stmt.setFloat(3,nuevoPedido.getPrecioTotal());
            stmt.setTimestamp(4, Timestamp.valueOf(nuevoPedido.getFechaPedido()));
            stmt.setString(5, nuevoPedido.getEstado());
            stmt.setInt(6, nuevoPedido.getMesaAsignada().getId());
            stmt.setInt(7, id);
            int filas = stmt.executeUpdate();
            if(filas==0){
                return null;
            }
            return nuevoPedido;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM pedido WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean cambiarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE pedido SET estado = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean cambiarMetodoPago(int id, String metodoPago) throws SQLException {
        String sql = "UPDATE pedido SET metodo = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, metodoPago);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedido";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public List<Producto> buscarProductos(int pedidoId) throws SQLException {
        String sql = "SELECT p.id, p.precio, p.descripcion, p.imagen, p.categoria, p.nombre " +
                "FROM producto p " +
                "JOIN producto_pedido pp ON pp.id_producto = p.id " +
                "WHERE pp.id_pedido = ?";
        List<Producto> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto(
                            rs.getInt("id"), rs.getFloat("precio"), rs.getString("descripcion"),
                            rs.getString("categoria"), rs.getString("nombre"));
                    producto.setImagenURL(rs.getString("imagen"));
                    resultado.add(producto);
                }
            }
        }
        return resultado;
    }

    @Override
    public boolean agregarProducto(int pedidoId, int productoId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                boolean agregado;
                String sqlInsert = "INSERT INTO producto_pedido (id_pedido, id_producto) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                    stmt.setInt(1, pedidoId);
                    stmt.setInt(2, productoId);
                    agregado = stmt.executeUpdate() > 0;
                }
                if (agregado) {
                    recalcularPrecioTotal(conn, pedidoId);
                }
                conn.commit();
                return agregado;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }

    }

    @Override
    public boolean quitarProducto(int pedidoId, int productoId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                boolean eliminado;
                String sqlDelete = "DELETE FROM producto_pedido WHERE id_pedido = ? AND id_producto = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
                    stmt.setInt(1, pedidoId);
                    stmt.setInt(2, productoId);
                    eliminado = stmt.executeUpdate() > 0;
                }
                if (eliminado) {
                    recalcularPrecioTotal(conn, pedidoId);
                }
                conn.commit();
                return eliminado;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    @Override
    public List<Empleado> buscarEncargados(int pedidoId) throws SQLException {
        String sql = "SELECT e.cedula, e.cargo, e.nombre, e.contrasena " +
                "FROM empleado e " +
                "JOIN empleado_pedido pe ON pe.ced_empleado = e.cedula " +
                "WHERE pe.id_pedido = ?";
        List<Empleado> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Empleado(
                            rs.getInt("cedula"), rs.getString("cargo"),
                            rs.getString("nombre"), rs.getString("contrasena")));
                }
            }
        }
        return resultado;
    }

    @Override
    public List<Pedido> buscarPorEncargado(int empleadoCedula) throws SQLException {
        String sql = "SELECT p.id, p.metodo, p.precio_total, p.fecha_pedido, p.estado, p.id_mesa " +
                "FROM pedido p " +
                "JOIN empleado_pedido pe ON pe.id_pedido = p.id " +
                "WHERE pe.ced_empleado = ?";
        List<Pedido> resultado = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleadoCedula);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearPedido(rs));
                }
            }
        }
        return resultado;
    }

    @Override
    public boolean agregarEncargado(int pedidoId, int empleadoCedula) throws SQLException {
        String sql = "INSERT INTO empleado_pedido (id_pedido, ced_empleado) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            stmt.setInt(2, empleadoCedula);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean quitarEncargado(int pedidoId, int empleadoCedula) throws SQLException {
        String sql = "DELETE FROM empleado_pedido WHERE id_pedido = ? AND ced_empleado = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            stmt.setInt(2, empleadoCedula);
            return stmt.executeUpdate() > 0;
        }
    }

    private Pedido mapearPedido(ResultSet rs)throws SQLException{
        Pedido pedido = new Pedido(
                rs.getInt("id"),
                rs.getTimestamp("fecha_pedido").toLocalDateTime(),
                new Mesa(rs.getInt("id_mesa"))
        );
        pedido.setEstado(rs.getString("estado"));
        pedido.setMetodoPago(rs.getString("metodo"));
        pedido.setPrecioTotal(rs.getFloat("precio_total"));
        return pedido;
    }

    private void recalcularPrecioTotal(Connection conn, int pedidoId) throws SQLException {
        String sqlSuma = "SELECT COALESCE(SUM(pr.precio), 0) AS total " +
                "FROM producto pr JOIN producto_pedido pp ON pp.id_producto = pr.id " +
                "WHERE pp.id_pedido = ?";
        float total;
        try (PreparedStatement stmt = conn.prepareStatement(sqlSuma)) {
            stmt.setInt(1, pedidoId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                total = rs.getFloat("total");
            }
        }
        String sqlUpdate = "UPDATE pedido SET precio_total = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setFloat(1, total);
            stmt.setInt(2, pedidoId);
            stmt.executeUpdate();
        }
    }
}
