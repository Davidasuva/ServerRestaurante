package server.model.empleado;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface EmpleadoInterface extends Remote {

    public Empleado registrar(Empleado empleado) throws RemoteException;
    public Empleado getEmpleadoByCedula(int cedula)throws RemoteException;
    public List<Empleado> getEmpleadosByCargo(String cargo)throws RemoteException;

    public List<Empleado> getEmpleadoByNombre(String nombre)throws RemoteException;

    public boolean removeEmpleado(int id)throws RemoteException;
    public Empleado modifyEmpleado(int id, Empleado empleado)throws RemoteException;

    public List<Empleado> getEmpleados(int inicio, int finalnum)throws RemoteException;
}
