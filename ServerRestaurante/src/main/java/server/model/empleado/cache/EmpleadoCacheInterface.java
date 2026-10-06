package server.model.empleado.cache;

import server.model.empleado.Empleado;

public interface EmpleadoCacheInterface {

    Empleado getEmpleadoByCedula(int cedula);
    Empleado addEmpleadoToCache(Empleado empleado);
    boolean removeEmpleadoFromCache(int cedula);
    boolean updateEmpleadoInCache(Empleado empleado);
}
