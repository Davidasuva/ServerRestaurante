package server.model.producto;

import server.model.history.History;
import server.model.ingrediente.Ingrediente;
import server.model.ingrediente.IngredienteInterface;
import server.model.producto.cache.ProductoCache;
import server.model.producto.cache.ProductoCacheInterface;
import server.model.producto.dao.ProductoDao;
import server.model.producto.dao.ProductoDaoInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;


public class ProductoService extends UnicastRemoteObject implements ProductoInterface {

    private ProductoDaoInterface productoDAO;
    private History history;
    private ProductoCacheInterface cache;
    private IngredienteInterface ingredienteService;

    public ProductoService(History history, IngredienteInterface ingredienteService) throws RemoteException {
        super();
        this.productoDAO = new ProductoDao();
        this.history = history;
        this.cache = new ProductoCache();
        this.ingredienteService = ingredienteService;
    }
    public void invalidarCache() {
        cache.clear();
    }

    @Override
    public Producto registrar(Producto producto) throws RemoteException {
        if (producto == null) {
            throw new RemoteException("Por favor añada un producto antes de registrarlo");
        }
        try {
            Producto creado = productoDAO.insertar(producto);
            cache.addProductoToCache(creado);
            history.addAction("Producto registrado con id: " + creado.getId());
            return creado;
        } catch (SQLException e) {
            throw new RemoteException("Error al registrar producto: " + e.getMessage());
        }
    }

    @Override
    public Producto getProductoById(int id) throws RemoteException {
        Producto cach=cache.getProductoById(id);
        if(cach==null){
            try {
                Producto producto = productoDAO.buscarPorId(id);
                if (producto == null) {
                    throw new RemoteException("Producto no encontrado");
                }
                List<Ingrediente> ingredientes = productoDAO.buscarIngredientes(id);
                producto.setIngredientes(ingredientes);
                cache.addProductoToCache(producto);
                history.addAction("Se buscó el producto con id: " + id);
                return producto;
            } catch (SQLException e) {
                throw new RemoteException("Error al buscar producto: " + e.getMessage());
            }
        }else{
            history.addAction("Producto buscado con id: " + id+" (en cache)");
            return cach;
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
        return getProductoById(id).getIngredientes();
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
        if (id != producto.getId()) {
            throw new RemoteException("No se puede cambiar el id de un producto");
        }
        try {
            Producto actualizado = productoDAO.actualizar(id, producto);
            if (actualizado == null) {
                throw new RemoteException("Producto no encontrado");
            }

            Producto enCache = cache.getProductoById(id);
            List<Ingrediente> ingredientes;
            if(enCache!=null){
                ingredientes=enCache.getIngredientes();
            }else{
                ingredientes=productoDAO.buscarIngredientes(id);
            }
            actualizado.setIngredientes(ingredientes);

            if (enCache == null) {
                cache.addProductoToCache(actualizado);
            } else {
                cache.updateProductoInCache(actualizado);
            }
            history.addAction("Se modificó el producto con id: " + id);
            return actualizado;
        } catch (SQLException e) {
            throw new RemoteException("Error al modificar producto: " + e.getMessage());
        }
    }

    @Override
    public boolean addIngredienteToProducto(int idProducto, int idIngrediente) throws RemoteException {
        if(idProducto <= 0 || idIngrediente <= 0) {
            throw new RemoteException("Por favor ingrese un id válido");
        }
        Producto producto= getProductoById(idProducto);
        Ingrediente ingrediente = ingredienteService.getIngredienteById(idIngrediente);
        try {
            boolean agregado = productoDAO.agregarIngrediente(idProducto, idIngrediente);
            if (agregado) {
                history.addAction("Se agregó el ingrediente " + idIngrediente + " al producto " + idProducto);
                producto.getIngredientes().add(ingrediente);
                cache.updateProductoInCache(producto);
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
                Producto producto = getProductoById(idProducto);
                producto.getIngredientes().removeIf(i -> i.getId() == idIngrediente);
                cache.updateProductoInCache(producto);
            }
            return eliminado;
        } catch (SQLException e) {
            throw new RemoteException("Error al quitar ingrediente: " + e.getMessage());
        }
    }

    @Override
    public Producto addImagenToProducto(int idProducto, String imagenURL) throws RemoteException {
        if (imagenURL == null || imagenURL.trim().isEmpty()) {
            throw new RemoteException("Por favor ingrese la URL de la imagen");
        }
        Producto producto = getProductoById(idProducto);
        try {
            String url = imagenURL.trim();
            productoDAO.actualizarImagen(idProducto, url);
            producto.setImagenURL(url);
            if(cache.getProductoById(idProducto)==null){
                cache.addProductoToCache(producto);
            }else{
                cache.updateProductoInCache(producto);
            }
            history.addAction("Se añadió/actualizó la imagen del producto con id: " + idProducto);
            return producto;
        } catch (SQLException e) {
            throw new RemoteException("Error al añadir imagen: " + e.getMessage());
        }
    }

    @Override
    public boolean removeImagenFromProducto(int idProducto) throws RemoteException {
        Producto producto= getProductoById(idProducto);
        try {
            boolean eliminada = productoDAO.eliminarImagen(idProducto);
            if (eliminada) {
                history.addAction("Se eliminó la imagen del producto con id: " + idProducto);
            }
            producto.setImagenURL(null);
            if(cache.getProductoById(idProducto)==null){
                cache.addProductoToCache(producto);
            }else{
                cache.updateProductoInCache(producto);
            }
            return eliminada;
        } catch (SQLException e) {
            throw new RemoteException("Error al eliminar imagen: " + e.getMessage());
        }
    }

    @Override
    public boolean removeProducto(int id) throws RemoteException {
        try {
            boolean eliminado = productoDAO.eliminar(id);
            if (eliminado) {
                cache.removeProductoFromCache(id);
                history.addAction("Se eliminó el producto con id: " + id);
            }
            return eliminado;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new RemoteException("No se puede eliminar el producto: está asociado a pedidos en su historial");
            }
            throw new RemoteException("Error al eliminar producto: " + e.getMessage());
        }
    }

    @Override
    public int contar() throws RemoteException {
        try {
            return productoDAO.contar();
        } catch (SQLException e) {
            throw new RemoteException("Error al contar productos: " + e.getMessage());
        }
    }
}
