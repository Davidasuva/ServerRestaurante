package server.model.ingrediente.cache;

import server.model.ingrediente.Ingrediente;

public interface IngredienteCacheInterface {

    Ingrediente getIngredienteById(int id);
    Ingrediente addIngredienteToCache(Ingrediente ingrediente);
    boolean removeIngredienteFromCache(int id);
    boolean updateIngredienteInCache(Ingrediente ingrediente);
    void clear();
}
