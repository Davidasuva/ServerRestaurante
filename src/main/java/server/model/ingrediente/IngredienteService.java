package server.model.ingrediente;

import server.model.history.History;

import java.util.List;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.TreeSet;

public class IngredienteService extends UnicastRemoteObject implements IngredienteInterface {

    private TreeSet<Ingrediente> ingredientes;
    private History history;
    public IngredienteService(History history) throws Exception {
        super();
        this.history = history;
        this.ingredientes = new TreeSet<>();
    }

    @Override
    public Ingrediente registrar(Ingrediente ingrediente) throws RemoteException {
        if(ingredientes==null){
            throw new RuntimeException("Por favor añada un ingrediente antes de registrarlo");
        }
        try{
            this.ingredientes.add(ingrediente);
            history.addAction("Se agregó el ingrediente con id: "+ingrediente.getId());
            return ingrediente;
        }catch (Exception e){
            throw new RemoteException("Error al registrar ingrediente: " + e.getMessage());
        }

    }

    @Override
    public List<Ingrediente> ingredientes() throws RemoteException {
        return ingredientes.stream().toList();
    }

    @Override
    public Ingrediente getIngredienteById(int id) throws RemoteException {
        Ingrediente ingrediente=new Ingrediente(id, "", 0,"");
        if(!ingredientes.contains(ingrediente)){
            throw new RemoteException("Ingrediente no encontrado");
        }
        history.addAction("Se buscó el ingrediente con id: "+ id);
        return ingredientes.ceiling(ingrediente);
    }

    @Override
    public Ingrediente getIngredienteByNombre(String nombre) throws RemoteException {
        if(nombre==null){
            throw new RemoteException("Por favor selecciona un nombre");
        }
        return ingredientes.stream().filter(e -> nombre.equals(e.getNombre())).findFirst().orElse(null);
    }

    @Override
    public Ingrediente modifyIngrediente(int id, Ingrediente nuevoIngrediente) throws RemoteException {
        if(nuevoIngrediente==null){
            throw new RemoteException("Por favor selecciona un ingrediente");
        }
        Ingrediente ingredienteExistente = getIngredienteById(id);
        ingredientes.remove(ingredienteExistente);
        ingredientes.add(nuevoIngrediente);
        history.addAction("Se modificó el ingrediente con id: "+ id);
        return nuevoIngrediente;
    }

    @Override
    public boolean removeIngrediente(int id) throws RemoteException {
        Ingrediente ingrediente = getIngredienteById(id);
        boolean eliminado= ingredientes.remove(ingrediente);
        if(eliminado){
            history.addAction("Se eliminó el ingrediente con id: "+id);
        }
        return eliminado;
    }
}
