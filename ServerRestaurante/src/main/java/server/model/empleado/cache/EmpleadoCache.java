package server.model.empleado.cache;

import server.model.empleado.Empleado;

import java.util.concurrent.ConcurrentHashMap;

public class EmpleadoCache implements EmpleadoCacheInterface {

    private final ConcurrentHashMap<Integer, Empleado> cacheEmpleados;

    public EmpleadoCache() {
        this.cacheEmpleados = new ConcurrentHashMap<>();
    }

    @Override
    public Empleado getEmpleadoByCedula(int cedula) {
        return cacheEmpleados.get(cedula);
    }

    @Override
    public Empleado addEmpleadoToCache(Empleado empleado) {
        if(empleado == null){
            return null;
        }
        return cacheEmpleados.putIfAbsent(empleado.getCedula(), empleado)==null?empleado:null;
    }

    @Override
    public boolean removeEmpleadoFromCache(int cedula) {
        return cacheEmpleados.remove(cedula) != null;
    }

    @Override
    public boolean updateEmpleadoInCache(Empleado empleado) {
        if(empleado == null){
            return false;
        }
        return cacheEmpleados.replace(empleado.getCedula(), empleado) != null;
    }
}
