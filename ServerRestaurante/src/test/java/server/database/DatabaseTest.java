package server.database;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTest {

    @Test
    void puedeConectarseALaBaseDeDatos() throws Exception {
        try (Connection conn = Database.getConnection()) {
            assertNotNull(conn, "getConnection() devolvió null");
            assertFalse(conn.isClosed(), "La conexión llegó cerrada");
            assertTrue(conn.isValid(5), "La conexión no respondió a un ping en 5 segundos");
        }
    }
}