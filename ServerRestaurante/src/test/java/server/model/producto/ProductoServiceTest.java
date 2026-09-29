package server.model.producto;

import org.junit.jupiter.api.Test;
import server.model.history.History;
import server.model.ingrediente.Ingrediente;
import server.model.ingrediente.IngredienteInterface;
import server.model.ingrediente.IngredienteService;

import java.rmi.RemoteException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoServiceTest {

    @Test
    void registrar() throws Exception {
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        Producto producto= new Producto(001,10000,"Pene en leche de la casa","Embutidos","Pene en lechita");
        try{
            Producto agregado= service.registrar(producto);
            assertNotNull(agregado);
            System.out.println(agregado);

        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }

    @Test
    void getProductoById() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            Producto agregado= service.getProductoById(1);
            assertNotNull(agregado);
            System.out.println(agregado);

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void getProductoByNombre() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            Producto agregado= service.getProductoByNombre("Pene en lechita");
            assertNotNull(agregado);
            System.out.println(agregado);

        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }

    @Test
    void getProductosByCategoria() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            List<Producto> agregado= service.getProductosByCategoria("Embutidos");
            assertNotNull(agregado);
            for(Producto p:agregado){
                System.out.println(p);
            }

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void getProductos() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            List<Producto> agregado= service.getProductos(0,0);
            assertNotNull(agregado);
            for(Producto p:agregado){
                System.out.println(p);
            }

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void getIngredientesPerProduct() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            List<Ingrediente> agregado= service.getIngredientesPerProduct(1);
            assertNotNull(agregado);
            for(Ingrediente p:agregado){
                System.out.println(p);
            }

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void validateProducto() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            boolean val=service.validateProducto(1);
            assertTrue(val);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void modifyProducto() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        Producto producto= new Producto(001,12000,"Pene en leche de la casa","Cagadera","Pene en lechita");
        try{
            Producto pro=service.modifyProducto(1,producto);
            assertNotNull(pro);
            System.out.println(pro);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void addIngredienteToProducto() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            boolean ing1= service.addIngredienteToProducto(1,1);
            boolean ing2= service.addIngredienteToProducto(1,3);
            boolean ing3= service.addIngredienteToProducto(1,4);
            assertTrue(ing1);
            assertTrue(ing2);
            assertTrue(ing3);

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void removeIngredienteFromProducto() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            boolean el=service.removeIngredienteFromProducto(1,4);
            assertTrue(el);

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void addImagenToProducto() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            Producto conImagen= service.addImagenToProducto(1,"https://ejemplo.com/imagenes/producto1.jpg");
            assertNotNull(conImagen);
            assertEquals("https://ejemplo.com/imagenes/producto1.jpg", conImagen.getImagenURL());
            assertEquals("https://ejemplo.com/imagenes/producto1.jpg", service.getProductoById(1).getImagenURL());

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void removeImagenFromProducto() throws Exception{
        History history=new History();
        ProductoInterface service= new ProductoService(history, new IngredienteService(history));
        try{
            service.addImagenToProducto(1,"https://ejemplo.com/imagenes/producto1.jpg");
            boolean el=service.removeImagenFromProducto(1);
            assertTrue(el);
            assertNull(service.getProductoById(1).getImagenURL());

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}