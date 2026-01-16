package demo.assertions;

import io.restassured.response.Response;

import static org.hamcrest.Matchers.*;

public final class ApiAssertions {
    private ApiAssertions() {}

    public static String extractToken(Response resp) {
        return resp.then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }

    public static int extractFirstProductId(Response resp) {
        return resp.then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .extract()
                .path("[0].id");
    }

    public static void assertUnauthorized(Response resp) {
        resp.then()
                .statusCode(401)
                .body("error", equalTo("unauthorized"));
    }

    public static void assertTokenExpired(Response resp) {
        resp.then()
                .statusCode(401)
                .body("error", equalTo("token_expired"));
    }

    public static void assertBadRequest(Response resp) {
        resp.then()
                .statusCode(400)
                .body("error", equalTo("bad_request"));
    }

    public static void assertAddToCartOk(Response resp) {
        resp.then()
                .statusCode(200)
                .body("cartId", equalTo("c-1"))
                .body("itemCount", equalTo(1));
    }

    public static String extractOrderIdCreated(Response resp) {
        return resp.then()
                .statusCode(200)
                .body("status", equalTo("CREATED"))
                .body("amount", equalTo(199))
                .extract()
                .path("orderId");
    }

    public static void assertOrderOk(Response resp, String expectedOrderId) {
        resp.then()
                .statusCode(200)
                .body("orderId", equalTo(expectedOrderId))
                .body("status", equalTo("CREATED"))
                .body("amount", equalTo(199));
    }

    public static void assertNotFound(Response resp) {
        resp.then()
                .statusCode(404)
                .body("error", equalTo("not_found"));
    }

    public static void assertConflictOutOfStock(Response resp) {
        resp.then()
                .statusCode(409)
                .body("error", equalTo("out_of_stock"));
    }

    public static void assertConflictDuplicate(Response resp) {
        resp.then()
                .statusCode(409)
                .body("error", equalTo("duplicate_submit"));
    }
}
