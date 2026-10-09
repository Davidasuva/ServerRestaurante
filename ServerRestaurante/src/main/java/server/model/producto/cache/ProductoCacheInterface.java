package server.model.producto.cache;

import server.model.producto.Producto;

public interface ProductoCacheInterface {

    Producto getProductoById(int id);
    Producto addProductoToCache(Producto producto);
    boolean removeProductoFromCache(int id);
    boolean updateProductoInCache(Producto producto);
    void clear();

}
