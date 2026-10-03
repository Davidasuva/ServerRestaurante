package server.model.pedido;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoService;
import server.model.history.History;
import server.model.ingrediente.IngredienteService;
import server.model.mesa.Mesa;
import server.model.mesa.MesaService;
import server.model.producto.Producto;
import server.model.producto.ProductoService;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Se ejecutan en orden (@Order): primero se registra el pedido y luego se prueban los demás métodos sobre él.
 * Usa datos que ya deben existir en la BD: mesa 1, empleado con cédula 100 y producto 1.
 * Si ya tienes un pedido con id 1, cambia ID_PEDIDO.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PedidoServiceTest {

    private static final int ID_PEDIDO = 1;
    private static final int ID_MESA = 1;
    private static final int CEDULA_EMPLEADO = 101;
    private static final int ID_PRODUCTO = 1;

    private PedidoService crearService() throws Exception {
        History history = new History();
        IngredienteService ingredienteService = new IngredienteService(history);
        ProductoService productoService = new ProductoService(history, ingredienteService);
        return new PedidoService(history, new MesaService(history), new EmpleadoService(history), productoService);
    }

    private Producto getProducto() throws Exception {
        History history = new History();
        return new ProductoService(history, new IngredienteService(history)).getProductoById(ID_PRODUCTO);
    }

    private Empleado getEmpleado() throws Exception {
        return new EmpleadoService(new History()).getEmpleadoByCedula(CEDULA_EMPLEADO);
    }

    @Test
    @Order(1)
    void registrarPedido() throws Exception {
        PedidoService service = crearService();
        Pedido pedido = new Pedido(ID_PEDIDO, LocalDateTime.now().withNano(0), new Mesa(ID_MESA));
        try {
            Pedido creado = service.registrarPedido(pedido);
            assertNotNull(creado, "registrarPedido() devolvió null");
            System.out.println(creado);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void getPedidoById() throws Exception {
        PedidoService service = crearService();
        try {
            Pedido pedido = service.getPedidoById(ID_PEDIDO);
            assertNotNull(pedido, "getPedidoById() devolvió null");
            System.out.println(pedido);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    void getPedidosPerFecha() throws Exception {
        PedidoService service = crearService();
        try {
            List<Pedido> pedidos = service.getPedidosPerFecha(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
            assertNotNull(pedidos, "getPedidosPerFecha() devolvió null");
            for (Pedido p : pedidos) {
                System.out.println(p);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(4)
    void getPedidosPerMesa() throws Exception {
        PedidoService service = crearService();
        try {
            List<Pedido> pedidos = service.getPedidosPerMesa(new Mesa(ID_MESA));
            assertNotNull(pedidos, "getPedidosPerMesa() devolvió null");
            for (Pedido p : pedidos) {
                System.out.println(p);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(5)
    void getPedidos() throws Exception {
        PedidoService service = crearService();
        try {
            List<Pedido> pedidos = service.getPedidos(0, 0);
            assertNotNull(pedidos, "getPedidos() devolvió null");
            for (Pedido p : pedidos) {
                System.out.println(p);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(6)
    void getPedidosPerEstado() throws Exception {
        PedidoService service = crearService();
        try {
            List<Pedido> pedidos = service.getPedidosPerEstado("Pendiente");
            assertNotNull(pedidos, "getPedidosPerEstado() devolvió null");
            for (Pedido p : pedidos) {
                System.out.println(p);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(7)
    void addProductoPerPedido() throws Exception {
        PedidoService service = crearService();
        try {
            boolean agregado = service.addProductoPerPedido(ID_PEDIDO, getProducto());
            assertTrue(agregado, "addProductoPerPedido() devolvió false");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(8)
    void getProductosPerPedido() throws Exception {
        PedidoService service = crearService();
        try {
            List<Producto> productos = service.getProductosPerPedido(ID_PEDIDO);
            assertNotNull(productos, "getProductosPerPedido() devolvió null");
            for (Producto p : productos) {
                System.out.println(p);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(9)
    void getPedidoPerPrecio() throws Exception {
        PedidoService service = crearService();
        try {
            List<Pedido> pedidos = service.getPedidoPerPrecio(0, 1000000);
            assertNotNull(pedidos, "getPedidoPerPrecio() devolvió null");
            for (Pedido p : pedidos) {
                System.out.println(p);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(10)
    void addEncargadoPerPedido() throws Exception {
        PedidoService service = crearService();
        try {
            boolean agregado = service.addEncargadoPerPedido(ID_PEDIDO, getEmpleado());
            assertTrue(agregado, "addEncargadoPerPedido() devolvió false");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(11)
    void getEncargadosPerPedido() throws Exception {
        PedidoService service = crearService();
        try {
            List<Empleado> encargados = service.getEncargadosPerPedido(ID_PEDIDO);
            assertNotNull(encargados, "getEncargadosPerPedido() devolvió null");
            for (Empleado e : encargados) {
                System.out.println(e);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(12)
    void getPedidosPerEncargado() throws Exception {
        PedidoService service = crearService();
        try {
            List<Pedido> pedidos = service.getPedidosPerEncargado(getEmpleado());
            assertNotNull(pedidos, "getPedidosPerEncargado() devolvió null");
            for (Pedido p : pedidos) {
                System.out.println(p);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(13)
    void setPaymentMethodPerPedido() throws Exception {
        PedidoService service = crearService();
        try {
            boolean cambiado = service.setPaymentMethodPerPedido(ID_PEDIDO, "Efectivo");
            assertTrue(cambiado, "setPaymentMethodPerPedido() devolvió false");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(14)
    void validatePedido() throws Exception {
        // Necesita que los ingredientes del producto 1 tengan stock
        PedidoService service = crearService();
        try {
            boolean valido = service.validatePedido(ID_PEDIDO);
            assertTrue(valido, "validatePedido() devolvió false");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(15)
    void modifyPedido() throws Exception {
        PedidoService service = crearService();
        try {
            Pedido pedido = service.getPedidoById(ID_PEDIDO);
            pedido.setMetodoPago("Tarjeta");
            Pedido modificado = service.modifyPedido(ID_PEDIDO, pedido);
            assertNotNull(modificado, "modifyPedido() devolvió null");
            System.out.println(modificado);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(16)
    void setPedidoStatus() throws Exception {
        PedidoService service = crearService();
        try {
            boolean cambiado = service.setPedidoStatus(ID_PEDIDO, "Cancelado");
            assertTrue(cambiado, "setPedidoStatus() devolvió false");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(17)
    void removeEncargadoPerPedido() throws Exception {
        PedidoService service = crearService();
        try {
            boolean eliminado = service.removeEncargadoPerPedido(ID_PEDIDO, getEmpleado());
            assertTrue(eliminado, "removeEncargadoPerPedido() devolvió false");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(18)
    void removeProductoPerPedido() throws Exception {
        PedidoService service = crearService();
        try {
            boolean eliminado = service.removeProductoPerPedido(ID_PEDIDO, getProducto());
            assertTrue(eliminado, "removeProductoPerPedido() devolvió false");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(19)
    void removePedido() throws Exception {
        PedidoService service = crearService();
        try {
            boolean eliminado = service.removePedido(ID_PEDIDO);
            assertTrue(eliminado, "removePedido() devolvió false");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}