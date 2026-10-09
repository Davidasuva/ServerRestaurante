package server.model.mesa.cache;

import server.model.mesa.Mesa;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MesaCache implements MesaCacheInterface{

    private final ConcurrentHashMap<Integer, Mesa> cacheMesas;

    public MesaCache() {
        this.cacheMesas = new ConcurrentHashMap<>();
    }

    @Override
    public Mesa getMesaById(int id) {
        return cacheMesas.get(id);
    }

    @Override
    public Mesa addMesaToCache(Mesa mesa) {
        if(mesa == null){
            return null;
        }
        return cacheMesas.putIfAbsent(mesa.getId(), mesa)==null?mesa:null;
    }

    @Override
    public boolean removeMesaFromCache(int id) {
        return cacheMesas.remove(id) != null;
    }

    @Override
    public boolean updateMesaInCache(Mesa mesa) {
        if(mesa == null){
            return false;
        }
        return cacheMesas.replace(mesa.getId(), mesa) != null;
    }

    @Override
    public List<Mesa> getAllMesas() {
        return cacheMesas.values().stream().toList();
    }
}
