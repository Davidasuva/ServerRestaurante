package server.model.mesa;

import server.model.history.History;
import server.model.mesa.cache.MesaCache;
import server.model.mesa.cache.MesaCacheInterface;
import server.model.mesa.dao.MesaDao;
import server.model.mesa.dao.MesaDaoInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.sql.SQLException;

public class MesaService extends UnicastRemoteObject implements MesaInterface {

    private MesaDaoInterface mesaDAO;
    private MesaCacheInterface mesaCache;
    private History history;

    public MesaService(History history) throws RemoteException {
        super();
        this.mesaDAO=new MesaDao();
        this.mesaCache=new MesaCache();
        this.history = history;
    }

    @Override
    public List<Mesa> getMesa(int inicio, int finalnum) throws RemoteException {
        if(inicio<0 || finalnum<0 || inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }
        try{
            int total= mesaDAO.contar();
            if(finalnum>=total){
                throw new RemoteException("El rango final no puede ser mayor al tamaño");
            }
            return mesaDAO.buscarTodas(inicio,finalnum);
        } catch (SQLException e) {
            throw new RemoteException("Error al consultar mesas: "+e.getMessage());
        }
    }

    @Override
    public Mesa registrar(Mesa mesa) throws RemoteException {
        if(mesa==null){
            throw new RemoteException("Por favor añada una mesa antes de registrarla");
        }
        try{
            Mesa creada =mesaDAO.insertar(mesa);
            mesaCache.addMesaToCache(creada);
            history.addAction("Se agregó la mesa con id: "+mesa.getId());
            return creada;
        }catch(SQLException e){
            throw new RemoteException("Error al registrar la mesa: "+e.getMessage());
        }

    }

    @Override
    public Mesa getMesaById(int id) throws RemoteException {
        Mesa mesa= mesaCache.getMesaById(id);
        if(mesa==null){
            try{
                Mesa mesa2= mesaDAO.buscarPorId(id);
                if(mesa2==null){
                    throw new RemoteException("No se encuentra la mesa");
                }
                mesaCache.addMesaToCache(mesa2);
                history.addAction("Se buscó la mesa con id: "+id);
                return mesa2;
            }catch(SQLException e){
                throw new RemoteException("Error al buscar mesa: "+e.getMessage());
            }
        }else{
            history.addAction("Se buscó la mesa con id: "+id+" (desde cache)");
            return mesa;
        }

    }

    @Override
    public Mesa modifyMesa(Mesa newMesa, int id) throws RemoteException {
        if(newMesa==null){
            throw new RemoteException("Por favor añada una mesa antes de modificarla");
        }
        if(newMesa.getId()!=id){
            throw new RemoteException("No se puede cambiar el id de una mesa");
        }
        try{
            Mesa actualizada=mesaDAO.actualizar(id,newMesa);
            if(actualizada==null){
                throw new RemoteException("No se encontró la mesa con id: "+id);
            }
            mesaCache.removeMesaFromCache(id);
            history.addAction("Se actualizo la mesa con id: "+id);
            return actualizada;
        } catch (SQLException e) {
            throw new RemoteException("Error al actualizar mesa: "+e.getMessage());
        }
    }

    @Override
    public boolean removeMesa(int id) throws RemoteException {
        try{
            boolean eliminado=mesaDAO.eliminar(id);
            if(eliminado){
                mesaCache.removeMesaFromCache(id);
                history.addAction("Se eliminó la mesa con id: "+id);
            }
            return eliminado;
        }catch (SQLException e){
            if ("23503".equals(e.getSQLState())) {
                throw new RemoteException("No se puede eliminar la mesa: tiene pedidos asociados en su historial");
            }
            throw new RemoteException("Error al eliminar mesa: " + e.getMessage());
        }
    }

    @Override
    public int contar()throws RemoteException{
        try{
            return mesaDAO.contar();
        }catch(SQLException e){
            throw new RemoteException("Error al consultar mesas: "+e.getMessage());
        }
    }
}
