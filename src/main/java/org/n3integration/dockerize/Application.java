package org.n3integration.dockerize;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static spark.Spark.get;

/**
 * Created by n3integration
 */
public class Application {

    static final String DEFAULT_USER = "World";

    public static void main(String[] args) {
        get("/", (req, res) -> String.format("Hello %s", getUsername()));
    }

    public static String getUsername() {
        // 1. check for presence of environment variable
        String user = System.getenv("USER");
        if(user == null) {
            // 2. load from properties file, if available
            Properties props = new Properties();
            try(InputStream instream = new FileInputStream("/data/application.properties")) {
                props.load(instream);
                return props.getProperty("user.name", DEFAULT_USER);
            }
            catch(IOException e) {
                return DEFAULT_USER;
            }
        }
        return user;
    }
}
