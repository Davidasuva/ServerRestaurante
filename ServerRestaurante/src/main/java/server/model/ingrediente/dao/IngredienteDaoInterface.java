package server.model.ingrediente.dao;

import server.model.ingrediente.Ingrediente;
import java.sql.SQLException;
import java.util.List;

public interface IngredienteDaoInterface {
    Ingrediente insertar(Ingrediente ingrediente)throws SQLException;
    List<Ingrediente> buscarTodos(int inicio, int finalnum)throws SQLException;
    Ingrediente buscarPorId(int id) throws SQLException;
    Ingrediente actualizar(int id, Ingrediente newIngrediente)throws SQLException;
    boolean eliminar(int id)throws SQLException;
    int contar()throws SQLException;
    Ingrediente getIngredienteByNombre(String nombre)throws SQLException;


}
