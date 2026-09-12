package server.model.empleado;

import server.model.history.History;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.TreeSet;

public class EmpleadoService extends UnicastRemoteObject implements  EmpleadoInterface {

    private TreeSet<Empleado> empleados;
    private History history;
    public EmpleadoService(History history) throws RemoteException {
        this.empleados = new TreeSet<>();
        this.history = history;
        super();
    }

    @Override
    public Empleado registrar(Empleado empleado) throws RemoteException {
        if(empleado==null){
            throw new RuntimeException("Por favor añada un empleado antes de registrarlo");
        }
        try{
            empleados.add(empleado);
            history.addAction("Se agregó el empleado con cédula: "+empleado.getCedula());
            return empleado;
        }catch (Exception e){
            throw new RemoteException("Error al registrar empleado: " + e.getMessage());
        }
    }

    @Override
    public Empleado getEmpleadoByCedula(int cedula) throws RemoteException{
        Empleado buscado =new Empleado(cedula, "", "","");
        if(!empleados.contains(buscado)){
            throw new RemoteException("Empleado no encontrado");
        }
        history.addAction("Se buscó el empleado con cédula: "+ cedula);
        return empleados.ceiling(buscado);
    }


    @Override
    public List<Empleado> getEmpleadosByCargo(String cargo) throws RemoteException{
        if(cargo==null){
            throw new RemoteException("Por favor selecciona un cargo");
        }
        return empleados.stream()
                .filter(e -> cargo.equals(e.getCargo()))
                .toList();
    }

    @Override
    public Empleado getEmpleadoByNombre(String nombre) throws RemoteException {
        if(nombre==null){
            throw new RemoteException("Por favor selecciona un nombre");
        }
        return empleados.stream().filter( e-> nombre.equals(e.getNombre())).findFirst().orElse(null);
    }

    @Override
    public boolean removeEmpleado(int id) throws RemoteException{
        Empleado empleado = getEmpleadoByCedula(id);
        boolean eliminado=empleados.remove(empleado);
        if(eliminado){
            history.addAction("Se eliminó el empleado con cédula: "+id);
        }
        return eliminado;
    }

    @Override
    public Empleado modifyEmpleado(int id, Empleado empleado) throws RemoteException{
        if(empleado==null){
            throw new RemoteException("Por favor selecciona un empleado");
        }
        Empleado cambiar=getEmpleadoByCedula(id);
        empleados.remove(cambiar);
        empleados.add(empleado);
        history.addAction("Se modificó el empleado con cédula: "+id);
        return empleado;

    }
}
