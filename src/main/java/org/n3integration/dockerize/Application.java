package org.n3integration.dockerize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.get;

/**
 * Created by n3integration
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class.getSimpleName());

    private final DataAccess dataAccess;

    public Application() {
        dataAccess = new DataAccess();
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.start();
    }

    public void start() {
        get("/", (req, res) -> {
            long start = System.currentTimeMillis();
            String username = getUsername();
            logger.info("{} - handling request for {} [{}ms]", Thread.currentThread().getId(), username, System.currentTimeMillis() - start);
            return String.format("Hello %s", username);
        });
    }

    public String getUsername() {
        return dataAccess.getUsername();
    }
}
