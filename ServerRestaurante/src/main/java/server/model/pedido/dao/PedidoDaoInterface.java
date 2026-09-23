package server.model.pedido.dao;

import server.model.empleado.Empleado;

import server.model.producto.Producto;
import server.model.pedido.Pedido;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


public interface PedidoDaoInterface {
    Pedido insertar(Pedido pedido) throws SQLException;

    Pedido buscarPorId(int id) throws SQLException;

    List<Pedido> buscarPorFecha(LocalDateTime fecha1, LocalDateTime fecha2) throws SQLException;

    List<Pedido> buscarPorMesa(int mesaId) throws SQLException;

    List<Pedido> buscarPorEstado(String estado) throws SQLException;

    List<Pedido> buscarPorRangoPrecio(float inicio, float finalnum) throws SQLException;

    List<Pedido> buscarTodos(int inicio, int finalnum) throws SQLException;

    Pedido actualizar(int id, Pedido nuevoPedido) throws SQLException;

    boolean eliminar(int id) throws SQLException;

    boolean cambiarEstado(int id, String estado) throws SQLException;

    boolean cambiarMetodoPago(int id, String metodoPago) throws SQLException;

    int contar() throws SQLException;


    List<Producto> buscarProductos(int pedidoId) throws SQLException;

    boolean agregarProducto(int pedidoId, int productoId) throws SQLException;

    boolean quitarProducto(int pedidoId, int productoId) throws SQLException;



    List<Empleado> buscarEncargados(int pedidoId) throws SQLException;

    List<Pedido> buscarPorEncargado(int empleadoCedula) throws SQLException;

    boolean agregarEncargado(int pedidoId, int empleadoCedula) throws SQLException;

    boolean quitarEncargado(int pedidoId, int empleadoCedula) throws SQLException;

}
