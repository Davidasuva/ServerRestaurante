package server.model.mesa;

import server.model.history.History;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.TreeSet;

public class MesaService extends UnicastRemoteObject implements MesaInterface {

    private TreeSet<Mesa> mesas;
    private History history;

    public MesaService(History history) throws RemoteException {
        this.mesas = new TreeSet<>();
        this.history = history;
        super();
    }

    @Override
    public List<Mesa> getMesa(int inicio, int finalnum) throws RemoteException {
        if(inicio<0 || finalnum<0 || inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }else if(finalnum>=mesas.size()){
            throw new RemoteException("El rango final no puede ser mayor al tamaño de la lista");
        }
        return mesas.stream().skip(inicio).limit(finalnum - inicio+1).toList();
    }

    @Override
    public Mesa registrar(Mesa mesa) throws RemoteException {
        if(mesa==null){
            throw new RuntimeException("Por favor añada una mesa antes de registrarla");
        }
        try{
            mesas.add(mesa);
            history.addAction("Se agregó la mesa con id: "+mesa.getId());
            return mesa;
        } catch (Exception e) {
            throw new RuntimeException("Error al registrar mesa: " + e.getMessage());
        }

    }

    @Override
    public Mesa getMesaById(int id) throws RemoteException {
        Mesa mesa= new Mesa(id);
        if(!mesas.contains(mesa)){
            throw new RemoteException("Mesa no encontrada");
        }

        history.addAction("Se buscó la mesa con id: "+ id);
        return mesas.ceiling(mesa);
    }

    @Override
    public Mesa modifyMesa(Mesa newMesa, int id) throws RemoteException {
        if(newMesa==null){
            throw new RuntimeException("Por favor añada una mesa antes de modificarla");
        }
        Mesa cambiar=getMesaById(id);
        mesas.remove((cambiar));
        mesas.add(newMesa);
        history.addAction("Se modificó la mesa con id: "+id);
        return newMesa;
    }

    @Override
    public boolean removeMesa(int id) throws RemoteException {
        Mesa mesa=getMesaById(id);
        boolean eliminado=mesas.remove(mesa);
        if(eliminado){
            history.addAction("Se eliminó la mesa con id: "+id);
        }
        return eliminado;
    }
}
