package server.model.producto;

import server.model.history.History;
import server.model.ingrediente.Ingrediente;
import server.model.ingrediente.IngredienteInterface;
import server.model.ingrediente.IngredienteService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.TreeSet;

public class ProductoService extends UnicastRemoteObject implements ProductoInterface {

    private TreeSet<Producto> productos;
    private History history;
    private IngredienteInterface ingredienteService;

    public ProductoService(History history, IngredienteInterface ingredienteService) throws RemoteException {
        super();
        this.productos = new TreeSet<>();
        this.history = history;
        this.ingredienteService = ingredienteService;
    }

    @Override
    public Producto registrar(Producto producto) throws RemoteException {
        if(productos==null){
            throw new RuntimeException("Por favor añada un producto antes de registrarlo");
        }
        try{
            productos.add(producto);
            history.addAction("Producto registrado: " + producto.getNombre());
            return producto;
        } catch (Exception e) {
            throw new RuntimeException("Error al registrar producto: " + e.getMessage());
        }
    }

    @Override
    public Producto getProductoById(int id) throws RemoteException {
        Producto producto =new Producto(id, 0, "", "", "");
        if(!productos.contains(producto)){
            throw new RemoteException("Producto no encontrado");
        }
        history.addAction("Se buscó el producto con id: " + id);
        return productos.ceiling(producto);
    }

    @Override
    public Producto getProductoByNombre(String nombre) throws RemoteException {
        if(nombre==null){
            throw new RemoteException("Por favor selecciona un nombre");
        }
        return productos.stream().filter(e -> nombre.equals(e.getNombre())).findFirst().orElse(null);
    }

    @Override
    public List<Producto> getProductosByCategoria(String categoria) throws RemoteException {
        if(categoria==null){
            throw new RemoteException("Por favor selecciona una categoría");
        }
        return productos.stream().filter(e-> categoria.equals(e.getCategoria())).toList();
    }

    @Override
    public List<Producto> getProductos(int inicio, int finalnum) throws RemoteException {
        if(inicio<0||finalnum<0||inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }else if(finalnum>=productos.size()){
            throw new RemoteException("El rango final no puede ser mayor al tamaño de la lista");
        }
        return productos.stream().skip(inicio).limit(finalnum - inicio+1).toList();
    }

    @Override
    public List<Ingrediente> getIngredientesPerProduct(int id) throws RemoteException {
        Producto producto=getProductoById(id);
        return producto.getIngredientes();
    }

    @Override
    public boolean validateProducto(int id) throws RemoteException {
        Producto producto = getProductoById(id);
        List<Ingrediente> ingredientes = producto.getIngredientes();
        for(Ingrediente i:ingredientes){
            if(i.getCantidad()<=0){
                return false;
            }
        }
        history.addAction("Se validó el producto con id: " + id);
        return true;
    }

    @Override
    public Producto modifyProducto(int id, Producto producto) throws RemoteException {
        Producto producto2 = getProductoById(id);
        if(producto==null){
            throw new RemoteException("Por favor añade el producto modificado");
        }
        productos.remove(producto2);
        productos.add(producto);
        history.addAction("Se modificó el producto con id: " + id);
        return producto;
    }

    @Override
    public boolean addIngredienteToProducto(int idProducto, int idIngrediente) throws RemoteException {
        Producto producto = getProductoById(idProducto);
        Ingrediente ingrediente = ingredienteService.getIngredienteById(idIngrediente);
        return producto.addIngrediente(ingrediente);
    }

    @Override
    public boolean removeIngredienteFromProducto(int idProducto, int idIngrediente) throws RemoteException {
        Producto producto = getProductoById(idProducto);
        Ingrediente ingrediente = ingredienteService.getIngredienteById(idIngrediente);
        return producto.removeIngrediente(ingrediente);
    }
}
