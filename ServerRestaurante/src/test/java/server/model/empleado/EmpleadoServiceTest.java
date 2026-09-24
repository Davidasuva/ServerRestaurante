package server.model.empleado;

import org.junit.jupiter.api.Test;
import server.model.history.History;

import java.rmi.RemoteException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmpleadoServiceTest {


    @Test
    void registrar() throws Exception {
        Empleado empleado=new Empleado(100,"Cocinero","Adrian","123");
        EmpleadoService service=new EmpleadoService(new History());
        try{
            Empleado creado=service.registrar(empleado);
            assertNotNull(creado,"Registrar() devolvió null");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getEmpleadoByCedula() throws RemoteException {
        EmpleadoService service=new EmpleadoService(new History());
        try{
            Empleado empleado=service.getEmpleadoByCedula(100);
            System.out.println(empleado);
            assertNotNull(empleado,"getEmpleadoByCedula() devolvió null");
        }catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getEmpleadosByCargo() throws RemoteException {
        EmpleadoService service=new EmpleadoService(new History());
        try{
            Empleado empleado=service.getEmpleadoByCedula(100);
            assertNotNull(empleado,"getEmpleadoByCedula() devolvió null");
            System.out.println(empleado);
        }catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getEmpleadoByNombre() throws RemoteException {
        EmpleadoService service=new EmpleadoService(new History());
        try{
            List<Empleado> empleados=service.getEmpleadoByNombre("Adrian");
            assertNotNull(empleados,"por nombre devolvió null");
            for(Empleado e: empleados)
                System.out.println(e);
        }catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void removeEmpleado() throws RemoteException {
        EmpleadoService service=new EmpleadoService(new History());
        try{
            boolean remove =service.removeEmpleado(100);
            assertTrue(remove,"Se eliminó correctamente");
        }catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void modifyEmpleado() throws RemoteException {
        EmpleadoService service=new EmpleadoService(new History());
        Empleado empleado=new Empleado(100,"Mesero","Juanse","234");
        try{
            Empleado prueba=service.modifyEmpleado(100,empleado);
            assertNotNull(prueba,"No se modifico el empleado");
            System.out.println(prueba);
        }catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getEmpleados() throws RemoteException {
        EmpleadoService service=new EmpleadoService(new History());
        Empleado empleado=new Empleado(103,"Cocinero","Julian","123");
        Empleado empleado2=new Empleado(101,"Mesero","Camilo","123");
        Empleado empleado3=new Empleado(102,"Mesero","Adrian","123");
        //Si quiere agregar los de arriba, coloca service.registrar(empleado) Si estos ya estan en la BD saldrá error, si no se agregaran normal
        try{
            List<Empleado> empleados=service.getEmpleados(0,3);
            assertNotNull(empleados,"La lista salio mala");
            for(Empleado e: empleados){
                System.out.println(e);
            }
        }catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}