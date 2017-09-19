package org.n3integration.dockerize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

/**
 * Created by n3integration
 */
public class DataAccess {

    private static final Logger logger = LoggerFactory.getLogger(DataAccess.class);

    static final String DRIVER_NAME  = "org.postgresql.Driver";
    static final String DEFAULT_USER = "postgres";
    static final String DEFAULT_URL  = "jdbc:postgresql://localhost/dockerize";
    static final String DEFAULT_PASS = "password";

    static {
        try {
            Class.forName(DRIVER_NAME);
        }
        catch(ClassNotFoundException e) {
            logger.error("Failed to initialize database driver.");
            logger.error("Please ensure that the driver from https://jdbc.postgresql.org/download.html is in your CLASSPATH.");
            System.exit(1);
        }
    }

    private static final ThreadLocal<Connection> threadConn = new ThreadLocal<Connection>() {
            @Override protected Connection initialValue() {
                try {
                    String url = getOrElse("DB_URL", DEFAULT_URL);
                    Properties props = new Properties();
                    props.setProperty("user", getOrElse("DB_USER", DEFAULT_USER));
                    props.setProperty("password", getOrElse("DB_PASS", DEFAULT_PASS));
                    final Connection conn = DriverManager.getConnection(url, props);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        closeQuietly(conn);
                    }));
                    return conn;
                }
                catch(SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };

    public String getUsername() {
        String query = "SELECT name FROM USERS LIMIT 1";
        String username = getOrElse("USER", "");
        try(PreparedStatement stmt = getConnection().prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            if(rs.next()) {
                username = rs.getString(1);
            }
        }
        catch(SQLException e) {
            // cleanup connection
            closeQuietly(getConnection());
            // fallback to cache if defined
            handleException(username, e);
        }
        catch(RuntimeException e) {
            // handle errors establishing connection
            handleException(username, e);
        }
        return username;
    }

    protected static void handleException(String defaultValue, Exception e) {
        if(defaultValue.equals("")) {
            throw new RuntimeException(e);
        }
    }

    protected static Connection getConnection() {
        return threadConn.get();
    }

    protected static String getOrElse(String property, String defaultValue) {
        String value = System.getenv(property);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    protected static void closeQuietly(Connection conn) {
        try {
            if(conn != null) {
                conn.close();
            }
        }
        catch(SQLException e) {
            /* ignore */
        }
    }
}
