package server.model.mesa;

import server.model.history.History;
import server.model.mesa.dao.MesaDao;
import server.model.mesa.dao.MesaDaoInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.sql.SQLException;

public class MesaService extends UnicastRemoteObject implements MesaInterface {

    private MesaDaoInterface mesaDAO;
    private History history;

    public MesaService(History history) throws RemoteException {
        this.mesaDAO=new MesaDao();
        this.history = history;
        super();
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
            throw new RuntimeException("Error al consultar mesas: "+e.getMessage());
        }
    }

    @Override
    public Mesa registrar(Mesa mesa) throws RemoteException {
        if(mesa==null){
            throw new RuntimeException("Por favor añada una mesa antes de registrarla");
        }
        try{
            Mesa creada =mesaDAO.insertar(mesa);
            history.addAction("Se agregó la mesa con id: "+mesa.getId());
            return creada;
        }catch(SQLException e){
            throw new RuntimeException("Error al registrar la mesa: "+e.getMessage());
        }

    }

    @Override
    public Mesa getMesaById(int id) throws RemoteException {
       try{
           Mesa mesa= mesaDAO.buscarPorId(id);
           if(mesa==null){
               throw new RemoteException("No se encuentra la mesa");
           }
           history.addAction("Se buscó la mesa con id: "+id);
           return mesa;
       }catch(SQLException e){
           throw new RuntimeException("Error al buscar mesa: "+e.getMessage());
       }
    }

    @Override
    public Mesa modifyMesa(Mesa newMesa, int id) throws RemoteException {
        if(newMesa==null){
            throw new RuntimeException("Por favor añada una mesa antes de modificarla");
        }
        try{
            Mesa actualizada=mesaDAO.actualizar(id,newMesa);
            history.addAction("Se actualizo la mesa con id: "+id);
            return actualizada;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar mesa: "+e.getMessage());
        }
    }

    @Override
    public boolean removeMesa(int id) throws RemoteException {
        try{
            boolean eliminado=mesaDAO.eliminar(id);
            if(eliminado){
                history.addAction("Se eliminó la mesa con id: "+id);
            }
            return eliminado;
        }catch (SQLException e){
            if (e.getErrorCode() == 1451) {
                throw new RemoteException("No se puede eliminar la mesa: tiene pedidos asociados en su historial");
            }
            throw new RemoteException("Error al eliminar mesa: " + e.getMessage());
        }
    }
}
