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
        if (connection == null || connection.isClosed()) {
            String uri = Environment.getInstance().getDatabase();
            System.out.println("Connecting to: " + uri);
            connection = DriverManager.getConnection(uri);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
