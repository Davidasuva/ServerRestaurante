package server.model.mesa;

import org.junit.jupiter.api.Test;
import server.model.history.History;

import java.rmi.RemoteException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MesaServiceTest {

    @Test
    void getMesa() throws RemoteException {
        MesaService service=new MesaService(new History());
        Mesa mesa=new Mesa(1);
        Mesa mesa2=new Mesa(2);
        Mesa mesa3=new Mesa(3);
        Mesa mesa4=new Mesa(4);
        //Si quiere agregar los de arriba, coloca service.registrar(mesa) Si estos ya estan en la BD saldrá error, si no se agregaran normal
        try{
            service.registrar(mesa);
            service.registrar(mesa2);
            service.registrar(mesa3);
            service.registrar(mesa4);
            List<Mesa> mesas=service.getMesa(0,3);
            assertNotNull(mesas,"Devolvió null");
            for(Mesa m:mesas){
                System.out.println(m);
            }
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }

    }

    @Test
    void registrar() throws RemoteException {
        Mesa mesa=new Mesa(1);
        MesaService service=new MesaService(new History());
        try{
            Mesa creado=service.registrar(mesa);
            assertNotNull(creado,"Devolvió null");
            System.out.println(creado);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void getMesaById() throws RemoteException {
        MesaService service=new MesaService(new History());
        try{
            Mesa buscado=service.getMesaById(1);
            assertNotNull(buscado,"Devolvió null");
            System.out.println(buscado);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void modifyMesa() throws RemoteException {
        MesaService service=new MesaService(new History());
        Mesa newMesa=new Mesa(2);
        try{
            Mesa buscado=service.modifyMesa(newMesa,1);
            assertNotNull(buscado,"Devolvió null");
            System.out.println(buscado);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void removeMesa() throws RemoteException {
        MesaService service=new MesaService(new History());
        try{
            boolean rem=service.removeMesa(2);
            assertTrue(rem,"Eliminado");
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }
}