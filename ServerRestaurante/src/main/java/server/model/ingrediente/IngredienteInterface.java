package server.model.ingrediente;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IngredienteInterface extends Remote {

    public Ingrediente registrar(Ingrediente ingrediente) throws RemoteException;
    public List<Ingrediente> getIngredientes(int inicio, int finalnum) throws RemoteException;
    public Ingrediente getIngredienteById(int id) throws RemoteException;
    public Ingrediente getIngredienteByNombre(String nombre) throws RemoteException;
    public Ingrediente modifyIngrediente(int id, Ingrediente nuevoIngrediente) throws RemoteException;
    public boolean removeIngrediente(int id) throws RemoteException;
    public boolean ajustarCantidad(int id, int agregado) throws RemoteException;
}
