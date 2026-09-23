package server.model.producto;

import server.model.history.History;
import server.model.ingrediente.Ingrediente;
import server.model.ingrediente.IngredienteInterface;
import server.model.producto.dao.ProductoDao;
import server.model.producto.dao.ProductoDaoInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;


public class ProductoService extends UnicastRemoteObject implements ProductoInterface {

    private ProductoDaoInterface productoDAO;
    private History history;
    private IngredienteInterface ingredienteService;

    public ProductoService(History history, IngredienteInterface ingredienteService) throws RemoteException {
        super();
        this.productoDAO = new ProductoDao();
        this.history = history;
        this.ingredienteService = ingredienteService;
    }

    @Override
    public Producto registrar(Producto producto) throws RemoteException {
        if (producto == null) {
            throw new RuntimeException("Por favor añada un producto antes de registrarlo");
        }
        try {
            Producto creado = productoDAO.insertar(producto);
            history.addAction("Producto registrado con id: " + producto.getId());
            return creado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar producto: " + e.getMessage());
        }
    }

    @Override
    public Producto getProductoById(int id) throws RemoteException {
        try {
            Producto producto = productoDAO.buscarPorId(id);
            if (producto == null) {
                throw new RemoteException("Producto no encontrado");
            }
            history.addAction("Se buscó el producto con id: " + id);
            return producto;
        } catch (SQLException e) {
            throw new RemoteException("Error al buscar producto: " + e.getMessage());
        }
    }

    @Override
    public Producto getProductoByNombre(String nombre) throws RemoteException {
        if (nombre == null) {
            throw new RemoteException("Por favor selecciona un nombre");
        }
        try {
            return productoDAO.buscarPorNombre(nombre);
        } catch (SQLException e) {
            throw new RemoteException("Error al buscar producto: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> getProductosByCategoria(String categoria) throws RemoteException {
        if (categoria == null) {
            throw new RemoteException("Por favor selecciona una categoría");
        }
        try {
            return productoDAO.buscarPorCategoria(categoria);
        } catch (SQLException e) {
            throw new RemoteException("Error al consultar productos: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> getProductos(int inicio, int finalnum) throws RemoteException {
        if (inicio < 0 || finalnum < 0 || inicio > finalnum) {
            throw new RemoteException("Por favor ingrese un rango válido");
        }
        try {
            int total = productoDAO.contar();
            if (finalnum >= total) {
                throw new RemoteException("El rango final no puede ser mayor al tamaño de la lista");
            }
            return productoDAO.buscarTodos(inicio, finalnum);
        } catch (SQLException e) {
            throw new RemoteException("Error al consultar productos: " + e.getMessage());
        }
    }

    @Override
    public List<Ingrediente> getIngredientesPerProduct(int id) throws RemoteException {
        try {
            return productoDAO.buscarIngredientes(id);
        } catch (SQLException e) {
            throw new RemoteException("Error al consultar ingredientes: " + e.getMessage());
        }
    }

    @Override
    public boolean validateProducto(int id) throws RemoteException {
        List<Ingrediente> ingredientes = getIngredientesPerProduct(id);
        for (Ingrediente i : ingredientes) {
            if (i.getCantidad() <= 0) {
                return false;
            }
        }
        history.addAction("Se validó el producto con id: " + id);
        return true;
    }

    @Override
    public Producto modifyProducto(int id, Producto producto) throws RemoteException {
        if (producto == null) {
            throw new RemoteException("Por favor añade el producto modificado");
        }
        try {
            Producto actualizado = productoDAO.actualizar(id, producto);
            if (actualizado == null) {
                throw new RemoteException("Producto no encontrado");
            }
            history.addAction("Se modificó el producto con id: " + id);
            return actualizado;
        } catch (SQLException e) {
            throw new RemoteException("Error al modificar producto: " + e.getMessage());
        }
    }

    @Override
    public boolean addIngredienteToProducto(int idProducto, int idIngrediente) throws RemoteException {
        getProductoById(idProducto);
        ingredienteService.getIngredienteById(idIngrediente);
        try {
            boolean agregado = productoDAO.agregarIngrediente(idProducto, idIngrediente);
            if (agregado) {
                history.addAction("Se agregó el ingrediente " + idIngrediente + " al producto " + idProducto);
            }
            return agregado;
        } catch (SQLException e) {
            throw new RemoteException("Error al asociar ingrediente: " + e.getMessage());
        }
    }

    @Override
    public boolean removeIngredienteFromProducto(int idProducto, int idIngrediente) throws RemoteException {
        try {
            boolean eliminado = productoDAO.quitarIngrediente(idProducto, idIngrediente);
            if (eliminado) {
                history.addAction("Se quitó el ingrediente " + idIngrediente + " del producto " + idProducto);
            }
            return eliminado;
        } catch (SQLException e) {
            throw new RemoteException("Error al quitar ingrediente: " + e.getMessage());
        }
    }
}
