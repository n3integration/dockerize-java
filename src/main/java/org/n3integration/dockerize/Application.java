package org.n3integration.dockerize;

import static spark.Spark.get;

/**
 * Created by n3integration
 */
public class Application {

    private final DataAccess dataAccess;

    public Application() {
        dataAccess = new DataAccess();
    }

    public static void main(String[] args) {
        Application app = new Application();
        get("/", (req, res) -> String.format("Hello %s", app.getUsername()));
    }

    public String getUsername() {
        return dataAccess.getUsername();
    }
}
