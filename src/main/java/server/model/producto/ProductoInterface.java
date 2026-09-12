package server.model.producto;

import server.model.ingrediente.Ingrediente;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
public interface ProductoInterface extends Remote {

    public Producto registrar(Producto producto) throws RemoteException;
    public Producto getProductoById(int id)throws RemoteException;
    public Producto getProductoByNombre(String nombre)throws RemoteException;
    public List<Producto> getProductosByCategoria(String categoria)throws RemoteException;
    public List<Producto> getProductos()throws RemoteException;

    public List<Ingrediente> getIngredientesPerProduct(int id)throws RemoteException;
    public boolean validateProducto(int id)throws RemoteException;
    public Producto modifyProducto(int id, Producto producto)throws RemoteException;

    public boolean addIngredienteToProducto(int idProducto, int idIngrediente)throws RemoteException;
    public boolean removeIngredienteFromProducto(int idProducto, int idIngrediente)throws RemoteException;




}
