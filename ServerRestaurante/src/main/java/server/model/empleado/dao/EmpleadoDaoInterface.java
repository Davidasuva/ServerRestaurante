package server.model.empleado.dao;
import server.model.empleado.Empleado;
import java.sql.SQLException;
import java.util.List;

public interface EmpleadoDaoInterface {

    Empleado insertar(Empleado empleado) throws SQLException;
    List<Empleado> buscarTodos(int inicio, int finalnum)throws SQLException;
    Empleado actualizar(int cedula, Empleado newEmpleado) throws SQLException;
    boolean eliminar(int id) throws SQLException;
    int contar() throws SQLException;
    Empleado buscarPorCedula(int cedula)throws SQLException;
    List<Empleado> buscarPorCargo(String cargo)throws SQLException;
    List<Empleado> buscarPorNombre(String nombre)throws SQLException;
}
