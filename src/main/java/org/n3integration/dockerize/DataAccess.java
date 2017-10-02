package org.n3integration.dockerize;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by n3integration
 */
public class DataAccess {

    private static final Logger logger = LoggerFactory.getLogger(DataAccess.class.getSimpleName());

    private static final String DEFAULT_URL  = "jdbc:postgresql://localhost/dockerize";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASS = "password";

    private final DataSource dataSource;

    public DataAccess() {
        this.dataSource = initializeDataSource();
    }

    public String getUsername() {
        String query = "SELECT name FROM USERS LIMIT 1";
        String username = getOrElse("USER", "");
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            if(rs.next()) {
                username = rs.getString(1);
            }
        }
        catch(SQLException | RuntimeException e) {
            // fallback to cache if defined
            handleException(username, e);
        }
        return username;
    }

    protected void handleException(String defaultValue, Exception e) {
        if(defaultValue.equals("")) {
            throw new RuntimeException(e);
        }
        else {
            logger.warn("failed to fetch username", e);
        }
    }

    protected Connection getConnection() {
        try {
            return dataSource.getConnection();
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String getOrElse(String property, String defaultValue) {
        String value = System.getenv(property);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    private static DataSource initializeDataSource() {
        do {
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(getOrElse("DB_URL", DEFAULT_URL));
                config.setUsername(getOrElse("DB_USER", DEFAULT_USER));
                config.setPassword(getOrElse("DB_PASS", DEFAULT_PASS));
                config.setMinimumIdle(50);
                config.setMaximumPoolSize(100);
                config.setConnectionTestQuery("SELECT 1");
                config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5));
                return new HikariDataSource(config);
            }
            catch(RuntimeException e) {
                try {
                    logger.warn("unable to initialize datasource: {}", e.getMessage());
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                    logger.debug("retrying datasource initialization");
                }
                catch(InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
        } while(true);
    }
}
