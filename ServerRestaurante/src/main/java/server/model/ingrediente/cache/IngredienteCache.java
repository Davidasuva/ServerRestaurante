package server.model.ingrediente.cache;

import server.model.ingrediente.Ingrediente;

import java.util.concurrent.ConcurrentHashMap;

public class IngredienteCache implements IngredienteCacheInterface{

    private final ConcurrentHashMap<Integer, Ingrediente> cache;

    public IngredienteCache() {
        this.cache=new ConcurrentHashMap<>();
    }
    @Override
    public Ingrediente getIngredienteById(int id) {
        return cache.get(id);
    }

    @Override
    public Ingrediente addIngredienteToCache(Ingrediente ingrediente) {

        if(ingrediente==null){
            return null;
        }
        return cache.putIfAbsent(ingrediente.getId(), ingrediente)==null?ingrediente:null;
    }

    @Override
    public boolean removeIngredienteFromCache(int id) {
        return cache.remove(id) != null;
    }

    @Override
    public boolean updateIngredienteInCache(Ingrediente ingrediente) {
        if(ingrediente==null){
            return false;
        }
        return cache.replace(ingrediente.getId(), ingrediente) != null;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
