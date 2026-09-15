package server.model.pedido;

import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoInterface;
import server.model.history.History;
import server.model.ingrediente.IngredienteService;
import server.model.mesa.Mesa;
import server.model.mesa.MesaInterface;
import server.model.producto.Producto;
import server.model.producto.ProductoInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeSet;

public class PedidoService extends UnicastRemoteObject implements PedidoInterface {
    private TreeSet<Pedido> pedidos;
    private History history;
    private MesaInterface mesaService;
    private EmpleadoInterface empleadoService;
    private ProductoInterface productoService;

    public PedidoService(History history, MesaInterface mesaService, EmpleadoInterface empleadoService, ProductoInterface productoService) throws Exception {
        super();
        this.history = history;
        this.pedidos = new TreeSet<>();
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
            this.pedidos.add(pedido);
            history.addAction("Se agregó el pedido con id: "+pedido.getId());
            return pedido;
        }catch (Exception e){
            throw new RemoteException("Error al registrar pedido: " + e.getMessage());
        }
    }

    @Override
    public Pedido getPedidoById(int id) throws RemoteException {
        Pedido pedido=new Pedido(id, LocalDateTime.now(), null);
        if(!pedidos.contains(pedido)){
            throw new RemoteException("Pedido no encontrado");
        }
        history.addAction("Se buscó el pedido con id: "+ id);
        return pedidos.ceiling(pedido);
    }

    @Override
    public List<Pedido> getPedidosPerFecha(LocalDateTime fecha1, LocalDateTime fecha2) throws RemoteException {
        if(fecha1==null || fecha2==null|| fecha1.isAfter(fecha2)){
            throw new RemoteException("Por favor ingrese fechas válidas");
        }

        return pedidos.stream().filter(p-> !p.getFechaPedido().isBefore(fecha1) && !p.getFechaPedido().isAfter(fecha2)).toList();
    }

    @Override
    public List<Pedido> getPedidosPerEncargado(Empleado encargado) throws RemoteException {
        if(encargado==null){
            throw new RemoteException("Por favor ingrese un encargado válido");
        }
        return pedidos.stream().filter(p->p.getEncargados().contains(encargado)).toList();
    }

    @Override
    public List<Pedido> getPedidosPerMesa(Mesa mesa) throws RemoteException {
        if(mesa==null){
            throw new RemoteException("Por favor ingrese una mesa válida");
        }
        return pedidos.stream().filter(p->p.getMesaAsignada().equals(mesa)).toList();
    }

    @Override
    public List<Pedido> getPedidos(int inicio, int finalnum) throws RemoteException {
        if(inicio<0 || finalnum<0 || inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }
        if(finalnum>pedidos.size()){
            throw new RemoteException("El rango final es mayor al tamaño de la lista de pedidos");
        }
        return pedidos.stream().skip(inicio).limit(finalnum-inicio+1).toList();

    }

    @Override
    public List<Pedido> getPedidosPerEstado(String estado) throws RemoteException {
        if(estado==null||estado.isEmpty()){
            throw new RemoteException("Por favor ingrese un estado válido");
        }
        return pedidos.stream().filter(p->p.getEstado().equals(estado)).toList();
    }

    @Override
    public List<Pedido> getPedidoPerPrecio(float inicio, float finalnum) throws RemoteException {
        if(inicio<0 || finalnum<0 || inicio>finalnum){
            throw new RemoteException("Por favor ingrese un rango válido");
        }

        return pedidos.stream().filter(p->p.getPrecioTotal()>=inicio&&p.getPrecioTotal()<=finalnum).toList();
    }

    @Override
    public boolean addProductoPerPedido(int id, Producto producto) throws RemoteException {
        if(producto==null){
            throw new RemoteException("Por favor ingrese un producto válido");
        }
        Pedido pedido =getPedidoById(id);
        history.addAction("Se modifico el pedido con id: "+id);
        return pedido.addProducto(producto);
    }

    @Override
    public boolean removeProductoPerPedido(int id, Producto producto) throws RemoteException {
        Pedido pedido=getPedidoById(id);
        if(pedido.getProductos().contains(producto)){
            history.addAction("Se modifico el pedido con id: "+id);
            return pedido.removeProducto(producto);
        }
        throw new RemoteException("El producto no se encuentra en el pedido con id: "+id);
    }

    @Override
    public boolean setPedidoStatus(int id, String estado) throws RemoteException {
        if(estado==null||estado.isEmpty()){
            throw new RemoteException("Por favor ingrese un estado válido");
        }
        Pedido pedido=getPedidoById(id);
        pedido.setEstado(estado);
        history.addAction("Se modifico el pedido con id: "+id);
        return true;
    }

    @Override
    public Pedido modifyPedido(int id, Pedido pedido) throws RemoteException {
        if(pedido==null){
            throw new RemoteException("Por favor ingrese un pedido válido");
        }
        Pedido cambiar=getPedidoById(id);
        pedidos.remove(cambiar);
        pedidos.add(pedido);
        history.addAction("Se modifico el pedido con id: "+id);
        return pedido;
    }

    @Override
    public boolean removePedido(int id) throws RemoteException {
        Pedido pedido=getPedidoById(id);
        boolean eliminado=pedidos.remove(pedido);
        if(eliminado){
            history.addAction("Se elimino al pedido con id: "+id);
        }
        return eliminado;
    }

    @Override
    public boolean addEncargadoPerPedido(int id, Empleado encargado) throws RemoteException {
        if(encargado==null){
            throw new RuntimeException("Debe seleccionar bien un empleado a añadir");
        }
        Pedido pedido=getPedidoById(id);
        boolean añadido= pedido.getEncargados().add(encargado);
        if(añadido){
            history.addAction("Se modifico el pedido con id: "+id);
        }

        return añadido;
    }

    @Override
    public boolean removeEncargadoPerPedido(int id, Empleado encargado) throws RemoteException {
        Pedido pedido=getPedidoById(id);
        if(!pedido.getEncargados().contains(encargado)){
            throw new RuntimeException("No se encuentra al encargado en ese pedido");
        }
        boolean eliminado= pedido.getEncargados().remove(encargado);
        if(eliminado){
            history.addAction("Se modifico el pedido con id: "+id);
        }
        return eliminado;
    }

    @Override
    public List<Producto> getProductosPerPedido(int id) throws RemoteException {
        Pedido pedido=getPedidoById(id);
        return pedido.getProductos();
    }

    @Override
    public List<Empleado> getEncargadosPerPedido(int id) throws RemoteException {
        Pedido pedido=getPedidoById(id);
        return pedido.getEncargados();
    }

    @Override
    public boolean setPaymentMethodPerPedido(int id, String metodoPago) throws RemoteException {
        if(metodoPago==null){
            throw new RemoteException("Seleccione un método de pago válido");
        }
        Pedido pedido=getPedidoById(id);
        pedido.setMetodoPago(metodoPago);
        history.addAction("Se modifico el pedido con id: "+id);
        return true;
    }

    @Override
    public boolean validatePedido(int id) throws RemoteException {
        Pedido pedido=getPedidoById(id);
        for()
        return false;
    }
}
