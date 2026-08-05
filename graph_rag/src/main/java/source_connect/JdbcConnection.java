package source_connect;
import java.sql.Connection;

public interface JdbcConnection extends DatabaseConnection {
    Connection getConnection();
}
