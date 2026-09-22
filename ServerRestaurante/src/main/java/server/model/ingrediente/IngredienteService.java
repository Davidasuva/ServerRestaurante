package server.model.ingrediente;

import server.model.history.History;
import server.model.ingrediente.dao.IngredienteDao;
import server.model.ingrediente.dao.IngredienteDaoInterface;


import java.sql.SQLException;
import java.util.List;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class IngredienteService extends UnicastRemoteObject implements IngredienteInterface {

    private IngredienteDaoInterface ingredienteDao;
    private History history;
    public IngredienteService(History history) throws Exception {
        super();
        this.history = history;
        this.ingredienteDao=new IngredienteDao();
    }

    @Override
    public Ingrediente registrar(Ingrediente ingrediente) throws RemoteException {
        if(ingrediente==null){
            throw new RuntimeException("Por favor añada un ingrediente antes de registrarlo");
        }
        try{
            Ingrediente crear=ingredienteDao.insertar(ingrediente);
            history.addAction("Se agregó un nuevo ingrediente con id: "+ingrediente.getId());
            return crear;
        }catch (SQLException e){
            throw new RuntimeException("Error al registrar ingrediente: " + e.getMessage());
        }

    }

    @Override
    public List<Ingrediente> getIngredientes(int inicio, int finalnum) throws RemoteException {
        if(inicio<0 || finalnum<0 || inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }
        try{
            return ingredienteDao.buscarTodos(inicio,finalnum);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar ingrediente: "+e.getMessage());
        }
    }

    @Override
    public Ingrediente getIngredienteById(int id) throws RemoteException {
        try{
            Ingrediente ingrediente=ingredienteDao.buscarPorId(id);
            history.addAction("Se buscó ingrediente por id: "+id);
            return ingrediente;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudó buscar ingrediente por id: "+e.getMessage());
        }
    }

    @Override
    public Ingrediente getIngredienteByNombre(String nombre) throws RemoteException {
        if(nombre==null){
            throw new RemoteException("Por favor selecciona un nombre");
        }
        try{
            Ingrediente ingrediente=ingredienteDao.getIngredienteByNombre(nombre);
            history.addAction("Se buscó ingrediente por nombre: "+nombre);
            return ingrediente;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudó buscar ingrediente por nombre: "+e.getMessage());
        }
    }

    @Override
    public Ingrediente modifyIngrediente(int id, Ingrediente nuevoIngrediente) throws RemoteException {
        if(nuevoIngrediente==null){
            throw new RemoteException("Por favor selecciona un ingrediente");
        }
        try{
            Ingrediente cambiado= ingredienteDao.actualizar(id,nuevoIngrediente);
            history.addAction("Se actualizó un ingrediente con id: "+id);
            return cambiado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar un ingrediente: "+e.getMessage());
        }
    }

    @Override
    public boolean removeIngrediente(int id) throws RemoteException {
        try{
            boolean eliminar=ingredienteDao.eliminar(id);
            if(eliminar){
                history.addAction("Se elimino el ingrediente con id: "+id);
            }
            return eliminar;
        }catch(SQLException e){
            throw new RuntimeException("Error al eliminar un ingrediente: "+e.getMessage());
        }
    }
}
