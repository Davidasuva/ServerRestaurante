package server.model.mesa;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MesaInterface extends Remote  {

    public List<Mesa> getMesa(int inicio, int finalnum) throws RemoteException;
    public Mesa registrar(Mesa mesa) throws RemoteException;
    public Mesa getMesaById(int id) throws RemoteException;
    public Mesa modifyMesa(Mesa newMesa, int id) throws RemoteException;
    public boolean removeMesa(int id) throws RemoteException;


}
