package server.model;

import server.model.empleado.EmpleadoInterface;
import server.model.empleado.EmpleadoService;
import server.model.history.History;
import server.model.ingrediente.IngredienteInterface;
import server.model.ingrediente.IngredienteService;
import server.model.mesa.MesaInterface;
import server.model.mesa.MesaService;
import server.model.pedido.PedidoInterface;
import server.model.pedido.PedidoService;
import server.model.producto.ProductoInterface;
import server.model.producto.ProductoService;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerModel {

    private final String ip;
    private final int port;
    private final String serviceName;
    private final String pedidoUri;
    private final String mesaUri;
    private final String empleadoUri;
    private final String ingredienteUri;
    private final String productoUri;

    private PedidoInterface pedidoService;
    private ProductoInterface productoService;
    private EmpleadoInterface empleadoService;
    private IngredienteInterface ingredienteService;
    private MesaInterface mesaService;
    private Registry registry;
    private final History history;


    public ServerModel(String ip, int port, String serviceName) {
        this.ip = ip;
        this.port = port;
        this.serviceName = serviceName;
        this.pedidoUri="//"+ip+":"+port+"/"+serviceName;
        this.mesaUri="//"+ip+":"+port+"/"+serviceName+"-mesas";
        this.empleadoUri="//"+ip+":"+port+"/"+serviceName+"-empleados";
        this.ingredienteUri="//"+ip+":"+port+"/"+serviceName+"-ingredientes";
        this.productoUri="//"+ip+":"+port+"/"+serviceName+"-productos";
        this.history=new History();

        history.addAction("ServerModel listo - URI base: "+pedidoUri);
    }

    public boolean deploy(){
        try{
            history.addAction("Iniciando despliegue en " + ip + ":" + port + "...");
            System.setProperty("java.rmi.server.hostname", ip);

            ingredienteService =new IngredienteService(history);
            empleadoService=new EmpleadoService(history);
            mesaService=new MesaService(history);
            productoService=new ProductoService(history,ingredienteService);
            pedidoService=new PedidoService(history,mesaService,empleadoService,productoService);

            registry = LocateRegistry.createRegistry(port);
            Naming.rebind(productoUri,productoService);
            Naming.rebind(pedidoUri,pedidoService);
            Naming.rebind(empleadoUri,empleadoService);
            Naming.rebind(mesaUri,mesaService);
            Naming.rebind(ingredienteUri, ingredienteService);

            history.addAction("PedidoService activo en: " + pedidoUri);
            history.addAction("ProductoService   activo en: " + productoUri);
            history.addAction("EmpleadoService   activo en: " + empleadoUri);
            history.addAction("MesaService   activo en: " + mesaUri);
            history.addAction("IngredienteService   activo en: " + ingredienteUri);

            return true;
        }catch (Exception e){
            history.addAction("Error al desplegar: "+ e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void stop(){
        tryUnbind(pedidoUri);
        tryUnbind(productoUri);
        tryUnbind(ingredienteUri);
        tryUnbind(empleadoUri);
        tryUnbind(mesaUri);
        tryUnexport(pedidoService, "PedidoService");
        tryUnexport(productoService,   "ProductoService");
        tryUnexport(mesaService,"MesaService");
        tryUnexport(empleadoService, "EmpleadoService");
        tryUnexport(ingredienteService, "IngredienteService");
        tryUnexport(registry,      "Registry (puerto " + port + ")");
    }

    private void tryUnbind(String uri) {
        try{
            Naming.unbind(uri);
            history.addAction("Binding eliminado: "+uri);
        } catch (Exception e) {
            history.addAction("Aviso unbind ("+uri+"): "+e.getMessage());
        }
    }

    private void tryUnexport(java.rmi.Remote obj, String name){
        if(obj == null){return;}
        try{
            UnicastRemoteObject.unexportObject(obj, true);
            history.addAction(name+" desexportado");
        }catch(Exception e){
            history.addAction("Aviso unexport "+name+": "+e.getMessage());
        }
    }
}
