package server.model.pedido;


import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoInterface;
import server.model.history.History;
import server.model.ingrediente.Ingrediente;
import server.model.mesa.Mesa;
import server.model.mesa.MesaInterface;
import server.model.pedido.dao.PedidoDao;
import server.model.pedido.dao.PedidoDaoInterface;
import server.model.producto.Producto;
import server.model.producto.ProductoInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;
public class PedidoService extends UnicastRemoteObject implements PedidoInterface {

    private static final String ESTADO_INICIAL = "Pendiente";
    private static final String ESTADO_CANCELADO = "Cancelado";

    private PedidoDaoInterface pedidoDao;
    private History history;
    private MesaInterface mesaService;
    private EmpleadoInterface empleadoService;
    private ProductoInterface productoService;

    public PedidoService(History history, MesaInterface mesaService, EmpleadoInterface empleadoService, ProductoInterface productoService) throws Exception {
        super();
        this.history = history;
        this.pedidoDao=new PedidoDao();
        this.mesaService=mesaService;
        this.productoService=productoService;
        this.empleadoService=empleadoService;
    }

    @Override
    public Pedido registrarPedido(Pedido pedido) throws RemoteException {
        if(pedido==null){
            throw new RemoteException("Por favor añada un pedido antes de registrarlo");
        }
        try{
            Pedido creado = pedidoDao.insertar(pedido);
            history.addAction("Se agrego el pedido con id: "+pedido.getId());
            return creado;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudó agregar el pedido: "+e.getMessage());
        }
    }

    @Override
    public Pedido getPedidoById(int id) throws RemoteException {
        try{
            Pedido pedido=pedidoDao.buscarPorId(id);
            if(pedido==null){
                throw new RuntimeException("No se encontró pedido por id: "+id);
            }
            history.addAction("Se busco pedido por id: "+id);
            return pedido;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido: "+e.getMessage());
        }
    }

    @Override
    public List<Pedido> getPedidosPerFecha(LocalDateTime fecha1, LocalDateTime fecha2) throws RemoteException {
        if(fecha1==null || fecha2==null|| fecha1.isAfter(fecha2)){
            throw new RemoteException("Por favor ingrese fechas válidas");
        }
        try{
            List<Pedido> pedidos=pedidoDao.buscarPorFecha(fecha1,fecha2);
            if(pedidos.isEmpty()){
                throw new RuntimeException("No se encontraron pedido por fecha: "+fecha1+" - "+fecha2);
            }
            history.addAction("Se busco pedido por fecha: "+fecha1+" - "+fecha2);
            return pedidos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido: "+e.getMessage());
        }
    }

    @Override
    public List<Pedido> getPedidosPerEncargado(Empleado encargado) throws RemoteException {
        if(encargado==null){
            throw new RemoteException("Por favor ingrese un encargado válido");
        }
        try{
            List<Pedido> pedidos=pedidoDao.buscarPorEncargado(encargado.getCedula());
            if(pedidos.isEmpty()){
                throw new RuntimeException("No se encontraron pedido por encargado: "+encargado);
            }
            history.addAction("Se busco pedido por encargado: "+encargado);
            return pedidos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido: "+e.getMessage());
        }
    }

    @Override
    public List<Pedido> getPedidosPerMesa(Mesa mesa) throws RemoteException {
        if(mesa==null){
            throw new RemoteException("Por favor ingrese una mesa válida");
        }
        try{
            List<Pedido> pedidos=pedidoDao.buscarPorMesa(mesa.getId());
            if(pedidos.isEmpty()){
                throw new RuntimeException("No se encontraron pedido por mesa: "+mesa);
            }
            history.addAction("Se busco pedido por mesa: "+mesa);
            return pedidos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido: "+e.getMessage());
        }
    }

    @Override
    public List<Pedido> getPedidos(int inicio, int finalnum) throws RemoteException {
        if(inicio<0 || finalnum<0 || inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }
        try{
            List<Pedido> pedidos=pedidoDao.buscarTodos(inicio,finalnum);
            if(pedidos.isEmpty()){
                throw new RuntimeException("No se encontraron pedidos");
            }
            history.addAction("Se buscaron pedidos");
            return pedidos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido: "+e.getMessage());
        }
    }

    @Override
    public List<Pedido> getPedidosPerEstado(String estado) throws RemoteException {
        if(estado==null||estado.isEmpty()){
            throw new RemoteException("Por favor ingrese un estado válido");
        }
        try{
            List<Pedido> pedidos=pedidoDao.buscarPorEstado(estado);
            if(pedidos.isEmpty()){
                throw new RuntimeException("No se encontraron pedidos por estado: "+estado);
            }
            history.addAction("Se buscaron pedidos por estado: "+estado);
            return pedidos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido: "+e.getMessage());
        }
    }

    @Override
    public List<Pedido> getPedidoPerPrecio(float inicio, float finalnum) throws RemoteException {
        if(inicio<0 || finalnum<0 || inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }
        try{
            List<Pedido> pedidos=pedidoDao.buscarPorRangoPrecio(inicio,finalnum);
            if(pedidos.isEmpty()){
                throw new RuntimeException("No se encontraron pedidos por rango precio: "+inicio+" - "+finalnum);
            }
            history.addAction("Se buscaron pedidos por rango precio: "+inicio+" - "+finalnum);
            return pedidos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido: "+e.getMessage());
        }
    }

    @Override
    public boolean addProductoPerPedido(int id, Producto producto) throws RemoteException {
        if(producto==null){
            throw new RemoteException("Porfavor añada un producto válido");
        }
        try{
            Pedido actual=pedidoDao.buscarPorId(id);
            if(actual==null){
                throw new RemoteException("No se encontró pedido por id: "+id);
            }
            boolean añadido;
            if(consumeInventario(actual.getEstado())){
                for(Ingrediente ing: productoService.getIngredientesPerProduct(producto.getId())){
                    if(ing.getCantidad()<1){
                        throw new RemoteException("Inventario insuficiente de '"+ing.getNombre()+"' para el producto "+producto.getId());
                    }
                }
                añadido=pedidoDao.agregarProductoDescontando(id,producto.getId());
                if(!añadido){
                    throw new RemoteException("No se pudo añadir el producto: el inventario cambió, intente de nuevo");
                }
            } else {
                añadido=pedidoDao.agregarProducto(id,producto.getId());
            }
            if(añadido){
                history.addAction("Se añadió el producto con id: "+producto.getId()+" Al pedido con id: "+id);
            }
            return añadido;
        } catch (SQLException e) {
            throw new RuntimeException("Error al agregar producto: "+e.getMessage());
        }
    }

    @Override
    public boolean removeProductoPerPedido(int id, Producto producto) throws RemoteException {
        if(producto==null){
            throw new RemoteException("Porfavor añada un producto válido");
        }
        try{
            boolean quitar=pedidoDao.quitarProducto(id,producto.getId());
            if(quitar){
                history.addAction("Se quitó el producto con id: "+producto.getId()+" Al pedido con id: "+id);
            }
            return quitar;
        } catch (SQLException e) {
            throw new RuntimeException("Error al quitar producto: "+e.getMessage());
        }
    }

    private boolean consumeInventario(String estado){
        return estado!=null
                && !ESTADO_INICIAL.equalsIgnoreCase(estado)
                && !ESTADO_CANCELADO.equalsIgnoreCase(estado);
    }

    @Override
    public boolean setPedidoStatus(int id, String estado) throws RemoteException {
        if(estado==null || estado.isEmpty()){
            throw new RemoteException("Seleccione un estado válido");
        }
        try{
            Pedido actual=pedidoDao.buscarPorId(id);
            if(actual==null){
                throw new RemoteException("No se encontró pedido por id: "+id);
            }
            boolean antes=consumeInventario(actual.getEstado());
            boolean despues=consumeInventario(estado);
            boolean cambiado;
            if(!antes && despues){
                validatePedido(id);
                cambiado=pedidoDao.cambiarEstadoDescontandoInventario(id,estado);
                if(!cambiado){
                    throw new RemoteException("No se pudo confirmar el pedido: el inventario cambió, intente de nuevo");
                }
                history.addAction("Se descontó el inventario del pedido con id: "+id);
            } else if(!despues){
                cambiado=pedidoDao.cambiarEstadoReponiendoInventario(id,estado);
                if(cambiado){
                    history.addAction("Se devolvió el inventario del pedido con id: "+id);
                }
            } else {
                cambiado=pedidoDao.cambiarEstado(id,estado);
            }
            if(cambiado){
                history.addAction("Se cambió el estado al pedido con id: "+id);
            }
            return cambiado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar estado del pedido: "+e.getMessage());
        }

    }

    @Override
    public Pedido modifyPedido(int id, Pedido pedido) throws RemoteException {
        if(pedido==null){
            throw new RemoteException("Actualize a un pedido válido");
        }
        try{
            if(pedido.getId()!=id){
                throw new RemoteException("No se puede cambiar el id de un pedido");
            }
            Pedido actual=pedidoDao.buscarPorId(id);
            if(actual!=null && consumeInventario(actual.getEstado())!=consumeInventario(pedido.getEstado())){
                throw new RemoteException("Para cambiar el estado use setPedidoStatus, así se actualiza el inventario");
            }
            Pedido act=pedidoDao.actualizar(id,pedido);
            history.addAction("Se actualizo el pedido con id: "+id);
            return act;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar pedido: "+e.getMessage());
        }

    }

    @Override
    public boolean removePedido(int id) throws RemoteException {
        try{
            boolean rem=pedidoDao.eliminar(id);
            if(rem){
                history.addAction("Se eliminó el pedido con id: "+id);
            }
            return rem;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar pedido: "+e.getMessage());
        }
    }

    @Override
    public boolean addEncargadoPerPedido(int id, Empleado encargado) throws RemoteException {
        if(encargado==null){
            throw new RemoteException("Verifique que el encargado es válido");
        }
        try{
            boolean add=pedidoDao.agregarEncargado(id,encargado.getCedula());
            if(add){
                history.addAction("Se agregó el encargado con cedula: "+encargado.getCedula()+" Al pedido con id: "+id);
            }
            return add;
        } catch (SQLException e) {
            throw new RuntimeException("Error al agregar encargado: "+e.getMessage());
        }
    }

    @Override
    public boolean removeEncargadoPerPedido(int id, Empleado encargado) throws RemoteException {
        if(encargado==null){
            throw new RemoteException("Verifique que el encargado es válido");
        }
        try{
            boolean elm=pedidoDao.quitarEncargado(id,encargado.getCedula());
            if(elm){
                history.addAction("Se eliminó el encargado con cedula: "+encargado.getCedula()+" Al pedido con id: "+id);
            }
            return elm;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar encargado: "+e.getMessage());
        }
    }

    @Override
    public List<Producto> getProductosPerPedido(int id) throws RemoteException {
        try{
            List<Producto> productos=pedidoDao.buscarProductos(id);
            if(productos.isEmpty()){
                throw new RuntimeException("No se encontraron productos en el pedido: "+id);
            }
            history.addAction("Se buscaron productos en el pedido: "+id);
            return productos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar productos: "+e.getMessage());
        }
    }

    @Override
    public List<Empleado> getEncargadosPerPedido(int id) throws RemoteException {
        try{
            List<Empleado> empleados=pedidoDao.buscarEncargados(id);
            if(empleados.isEmpty()){
                throw new RuntimeException("No se encontraron empleados en el pedido: "+id);
            }
            history.addAction("Se buscaron empleados en el pedido: "+id);
            return empleados;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar empleados: "+e.getMessage());
        }
    }

    @Override
    public boolean setPaymentMethodPerPedido(int id, String metodoPago) throws RemoteException {
        if(metodoPago==null){
            throw new RemoteException("Seleccione un método válido");
        }
        try{
            boolean cambiado=pedidoDao.cambiarMetodoPago(id,metodoPago);
            if(cambiado){
                history.addAction("Se cambió el método de pago al producto con id: "+id);
            }
            return cambiado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar método de pago del producto: "+e.getMessage());
        }

    }

    @Override
    public boolean validatePedido(int id) throws RemoteException {
        List<Producto> productos;
        try{
            productos=pedidoDao.buscarProductos(id);
        } catch (SQLException e) {
            throw new RemoteException("Error al validar pedido: "+e.getMessage());
        }
        if(productos.isEmpty()){
            throw new RemoteException("El pedido "+id+" no tiene productos");
        }


        Map<Integer,Integer> requerido=new HashMap<>();
        Map<Integer,Ingrediente> inventario=new HashMap<>();
        for(Producto p: productos){
            for(Ingrediente ing: productoService.getIngredientesPerProduct(p.getId())){
                requerido.merge(ing.getId(),1,Integer::sum);
                inventario.putIfAbsent(ing.getId(),ing);
            }
        }

        Map<Integer,Integer> reservado;
        try{
            reservado=pedidoDao.buscarInventarioReservado(id);
        } catch (SQLException e) {
            throw new RemoteException("Error al validar pedido: "+e.getMessage());
        }

        for(Map.Entry<Integer,Integer> e: requerido.entrySet()){
            Ingrediente ing=inventario.get(e.getKey());
            int faltante=e.getValue()-reservado.getOrDefault(e.getKey(),0);
            if(faltante>0 && ing.getCantidad()<faltante){
                throw new RemoteException("Inventario insuficiente de '"+ing.getNombre()+"': hay "
                        +ing.getCantidad()+" y se necesitan "+faltante);
            }
        }
        history.addAction("Se validó el pedido con id: "+id);
        return true;
    }
}
