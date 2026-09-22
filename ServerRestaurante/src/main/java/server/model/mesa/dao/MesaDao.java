package server.model.mesa.dao;
import server.database.Database;
import server.model.mesa.Mesa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MesaDao implements MesaDaoInterface{

    @Override
    public Mesa insertar(Mesa mesa) throws SQLException {
        String sql= "INSERT INTO mesa (id) VALUES (?)";
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1,mesa.getId());
            stmt.executeUpdate();
            return mesa;
        }
    }

    @Override
    public Mesa buscarPorId(int id) throws SQLException {
        String sql="SELECT id FROM mesa WHERE id=?";
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1,id);
            try(ResultSet rs=stmt.executeQuery()){
                if(rs.next()){
                    return mapearMesa(rs);
                }
                return null;
            }

        }
    }

    @Override
    public List<Mesa> buscarTodas(int inicio, int finalnum) throws SQLException {
        String sql = "SELECT id FROM mesa ORDER BY id LIMIT ? OFFSET ?";
        List<Mesa> mesas = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int limite = finalnum - inicio + 1;
            stmt.setInt(1, limite);
            stmt.setInt(2, inicio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mesas.add(mapearMesa(rs));
                }
            }
        }
        return mesas;
    }

    @Override
    public Mesa actualizar(int id, Mesa nuevaMesa) throws SQLException {
        String sql = "UPDATE mesa SET id = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuevaMesa.getId());
            stmt.setInt(2, id);
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                return null;
            }
            return nuevaMesa;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql="DELETE FROM mesa WHERE id=?";
        try(Connection conn=Database.getConnection(); PreparedStatement stmt=conn.prepareStatement(sql)){
            stmt.setInt(1,id);
            int filasAfectadas=stmt.executeUpdate();
            return filasAfectadas>0;
        }

    }

    @Override
    public int contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM mesa";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Mesa mapearMesa(ResultSet rs) throws SQLException {
        return new Mesa(rs.getInt("id"));
    }
}
