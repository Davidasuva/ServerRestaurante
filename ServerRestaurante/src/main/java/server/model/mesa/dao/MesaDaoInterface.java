package server.model.mesa.dao;

import server.model.mesa.Mesa;

import java.sql.SQLException;
import java.util.List;


public interface MesaDaoInterface {
    Mesa insertar(Mesa mesa) throws SQLException;

    Mesa buscarPorId(int id) throws SQLException;

    List<Mesa> buscarTodas(int inicio, int finalnum) throws SQLException;

    Mesa actualizar(int id, Mesa nuevaMesa) throws SQLException;

    boolean eliminar(int id) throws SQLException;

    int contar() throws SQLException;
}
