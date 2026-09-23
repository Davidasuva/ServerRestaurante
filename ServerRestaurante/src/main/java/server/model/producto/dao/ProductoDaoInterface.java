package server.model.producto.dao;
import server.model.ingrediente.Ingrediente;
import server.model.producto.Producto;

import java.sql.SQLException;
import java.util.List;

public interface ProductoDaoInterface {

    Producto insertar(Producto producto) throws SQLException;
    Producto buscarPorId(int id)throws SQLException;
    Producto buscarPorNombre(String nombre)throws SQLException;
    List<Producto> buscarPorCategoria(String categoria)throws SQLException;
    List<Producto> buscarTodos(int inicio, int finalnum)throws SQLException;
    Producto actualizar(int id, Producto newProducto)throws SQLException;
    boolean eliminar(int id)throws SQLException;
    int contar()throws SQLException;

    List<Ingrediente> buscarIngredientes(int productoId)throws SQLException;
    boolean agregarIngrediente(int productoId,int ingredienteId)throws SQLException;
    boolean quitarIngrediente(int productoId, int ingredienteId)throws SQLException;
}
