package server.model.ingrediente;

import org.junit.jupiter.api.Test;
import server.model.history.History;

import java.rmi.RemoteException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngredienteServiceTest {

    @Test
    void registrar() throws Exception {
        Ingrediente ingrediente=new Ingrediente(1,"Pene en salsa",10,"Pene");
        IngredienteService service=new IngredienteService(new History());
        try{
            Ingrediente in=service.registrar(ingrediente);
            assertNotNull(in,"devolvió null");
            System.out.println(in);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void getIngredientes() throws Exception {
        IngredienteService service=new IngredienteService(new History());
        Ingrediente ingrediente1=new Ingrediente(1,"Pene en salsa",10,"Pene");
        Ingrediente ingrediente2=new Ingrediente(2,"Orto en ortacha",7,"Ortacha");
        Ingrediente ingrediente3=new Ingrediente(3,"Leche con un toque de la casa",2,"Leche?");
        Ingrediente ingrediente4=new Ingrediente(4,"Aceite de motor especial de la casa",5,"Aceite");
        //Si quiere agregar los de arriba, coloca service.registrar(ingredienter) Si estos ya estan en la BD saldrá error, si no se agregaran normal
        try{
            service.registrar(ingrediente1);
            service.registrar(ingrediente2);
            service.registrar(ingrediente3);
            service.registrar(ingrediente4);

            List<Ingrediente> ingredientes=service.getIngredientes(0,3);
            assertNotNull(ingredientes);
            for(Ingrediente i: ingredientes){
                System.out.println(i);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getIngredienteById() throws Exception {
        IngredienteService service=new IngredienteService(new History());
        try{
            Ingrediente enc=service.getIngredienteById(3);
            assertNotNull(enc);
            System.out.println(enc);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getIngredienteByNombre() throws Exception {
        IngredienteService service=new IngredienteService(new History());
        try{
            Ingrediente enc=service.getIngredienteByNombre("Leche?");
            assertNotNull(enc);
            System.out.println(enc);

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void modifyIngrediente() throws Exception {
        IngredienteService service=new IngredienteService(new History());
        Ingrediente ingrediente=new Ingrediente(3,"Leche con un toque de la casa",2,"Lechita?");
        try{
            Ingrediente ing=service.modifyIngrediente(3,ingrediente);
            assertNotNull(ing);
            System.out.println(ing);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void removeIngrediente() throws Exception {
        IngredienteService service=new IngredienteService(new History());
        try{
            boolean rem=service.removeIngrediente(1);
            assertTrue(rem);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void ajustarCantidad() throws Exception {
        IngredienteService service=new IngredienteService(new History());
        try{
            boolean agr=service.ajustarCantidad(3,10);
            assertTrue(agr);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}