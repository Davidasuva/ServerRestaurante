package server.model.empleado;

import server.model.empleado.dao.EmpleadoDao;
import server.model.empleado.dao.EmpleadoDaoInterface;
import server.model.history.History;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

public class EmpleadoService extends UnicastRemoteObject implements  EmpleadoInterface {

    private EmpleadoDaoInterface empleadoDao;
    private History history;

    public EmpleadoService(History history) throws RemoteException {
        this.empleadoDao=new EmpleadoDao();
        this.history = history;
        super();
    }

    @Override
    public Empleado registrar(Empleado empleado) throws RemoteException {
        if(empleado==null){
            throw new RuntimeException("Por favor añada un empleado antes de registrarlo");
        }
        try{
            Empleado creado= empleadoDao.insertar(empleado);
            history.addAction("Se agrego el empleado con cedula: "+empleado.getCedula());
            return empleado;
        }catch(SQLException e){
            throw new RuntimeException("Error al registrar empleado. "+e.getMessage());
        }
    }

    @Override
    public Empleado getEmpleadoByCedula(int cedula) throws RemoteException{
        try{
            Empleado empleado=empleadoDao.buscarPorCedula(cedula);
            if(empleado==null){
                throw new RemoteException("No se encontro empleado con cedula: "+cedula);
            }
            history.addAction("Se buscó el empleado con cedula: "+cedula);
            return empleado;
        }catch(SQLException e){
            throw new RuntimeException("Error al buscar el empleado: "+e.getMessage());
        }
    }


    @Override
    public List<Empleado> getEmpleadosByCargo(String cargo) throws RemoteException{
        if(cargo==null){
            throw new RuntimeException("Por favor agregue cargo a buscar");
        }
        try{
            return empleadoDao.buscarPorCargo(cargo);
        }catch(SQLException e){
            throw new RuntimeException("Error al consultar empleados por cargo: "+e.getMessage());
        }
    }

    @Override
    public List<Empleado> getEmpleadoByNombre(String nombre) throws RemoteException {
        if(nombre==null){
            throw new RemoteException("Por favor selecciona un nombre");
        }
        try{
            return empleadoDao.buscarPorNombre(nombre);
        }catch(SQLException e){
            throw new RuntimeException("Error al consultar empleados por nombre: "+e.getMessage());
        }
    }

    @Override
    public boolean removeEmpleado(int id) throws RemoteException{
        try{
            boolean eliminar=empleadoDao.eliminar(id);
            if(eliminar){
                history.addAction("Se eliminó la mesa con id: "+id);
            }
            return eliminar;
        }catch(SQLException e){
            throw new RuntimeException("No se pudó eliminar el empleado: "+e.getMessage());
        }
    }

    @Override
    public Empleado modifyEmpleado(int id, Empleado empleado) throws RemoteException{
        if(empleado==null){
            throw new RemoteException("Por favor selecciona un empleado");
        }
        try{
            Empleado actualizada=empleadoDao.actualizar(id,empleado);
            history.addAction("Se actualizó el empleado con id: "+id);
            return actualizada;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar empleado: "+e.getMessage());
        }
    }

    @Override
    public List<Empleado> getEmpleados(int inicio, int finalnum) throws RemoteException {
        if(inicio<0 || finalnum<0 || inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }
        try{
            int total=empleadoDao.contar();
            if(finalnum>=total){
                throw new RemoteException("El rango final no puede ser mayor al número de empleados");
            }
            return empleadoDao.buscarTodos(inicio,finalnum);
        }catch(SQLException e){
            throw  new RuntimeException("Error al consultar empleados: "+e.getMessage());
        }
    }
}
