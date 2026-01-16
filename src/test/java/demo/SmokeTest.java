package demo;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class SmokeTest extends TestBase {


    @Test
    void healthCheck_publicApi() {
        given()
          .baseUri("https://jsonplaceholder.typicode.com")
        .when()
          .get("/posts/1")
        .then()
          .statusCode(200)
          .body("id", equalTo(1));
    }
}
