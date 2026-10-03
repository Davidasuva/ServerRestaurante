package server.database;
import environment.Environment;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URI = Environment.getInstance().getDatabase();

    private static Connection connection = null;

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Environment.getInstance().getDatabase());
    }

    public static void closeConnection() {
    }
}
