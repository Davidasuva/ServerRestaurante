package server.model.mesa.cache;

import server.model.mesa.Mesa;

import java.util.List;

public interface MesaCacheInterface {


    Mesa getMesaById(int id);
    Mesa addMesaToCache(Mesa mesa);
    boolean removeMesaFromCache(int id);
    boolean updateMesaInCache(Mesa mesa);

    List<Mesa> getAllMesas();

}
