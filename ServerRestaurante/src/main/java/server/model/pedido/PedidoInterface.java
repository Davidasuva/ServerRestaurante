package server.model.pedido;

import server.model.empleado.Empleado;
import server.model.mesa.Mesa;
import server.model.producto.Producto;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoInterface extends Remote {

    Pedido registrarPedido(Pedido pedido) throws RemoteException;
    Pedido getPedidoById(int id) throws RemoteException;
    List<Pedido> getPedidosPerFecha(LocalDateTime fecha1, LocalDateTime fecha2) throws RemoteException;

    List<Pedido> getPedidosPerEncargado(Empleado encargado) throws RemoteException;

    List<Pedido> getPedidosPerMesa(Mesa mesa) throws RemoteException;

    List<Pedido> getPedidos(int inicio, int finalnum) throws RemoteException;

    List<Pedido> getPedidosPerEstado(String estado) throws RemoteException;
    List<Pedido> getPedidoPerPrecio(float inicio, float finalnum) throws RemoteException;

    boolean addProductoPerPedido(int id, Producto producto)throws RemoteException;
    boolean removeProductoPerPedido(int id, Producto producto)throws RemoteException;

    boolean setPedidoStatus(int id, String estado)throws RemoteException;
    Pedido modifyPedido(int id, Pedido pedido)throws RemoteException;
    boolean removePedido(int id)throws RemoteException;

    boolean  addEncargadoPerPedido(int id, Empleado encargado)throws RemoteException;
    boolean removeEncargadoPerPedido(int id, Empleado encargado)throws RemoteException;

    List<Producto> getProductosPerPedido(int id)throws RemoteException;

    List<Empleado> getEncargadosPerPedido(int id)throws RemoteException;
    boolean setPaymentMethodPerPedido(int id, String metodoPago)throws RemoteException;

    boolean validatePedido(int id) throws RemoteException;

}
