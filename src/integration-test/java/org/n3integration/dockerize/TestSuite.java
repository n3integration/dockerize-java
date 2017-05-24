package org.n3integration.dockerize;

import org.junit.Test;

import static io.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.*;

/**
 * Created by n3integration
 */
public class TestSuite {
    @Test
    public void testApplication() {
        expect().body(containsString("Hello n3integration")).when().get("http://localhost:4567/");
    }
}
