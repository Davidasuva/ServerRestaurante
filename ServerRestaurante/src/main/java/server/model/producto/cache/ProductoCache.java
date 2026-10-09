package server.model.producto.cache;

import server.model.producto.Producto;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class ProductoCache implements ProductoCacheInterface{

    private final ConcurrentHashMap<Integer, Producto> cache;

    public ProductoCache() {
        this.cache=new ConcurrentHashMap<>();
    }

    @Override
    public Producto getProductoById(int id) {
        return cache.get(id);
    }

    @Override
    public Producto addProductoToCache(Producto producto) {
        if(producto==null){
            return null;
        }
        return cache.putIfAbsent(producto.getId(), producto)==null?producto:null;
    }

    @Override
    public boolean removeProductoFromCache(int id) {
        return cache.remove(id) != null;
    }

    @Override
    public boolean updateProductoInCache(Producto producto) {
        if(producto==null){
            return false;
        }
        return cache.replace(producto.getId(), producto) != null;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
