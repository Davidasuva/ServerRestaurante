package server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import environment.Environment;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static HikariDataSource dataSource;

    private Database() {}

    private static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(Environment.getInstance().getDatabase());
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setMaxLifetime(300_000);
            config.setKeepaliveTime(120_000);
            config.setConnectionTimeout(10_000);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static synchronized void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}