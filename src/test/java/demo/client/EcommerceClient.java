package demo.client;

import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class EcommerceClient {
    private final String baseUrl;
    private String token;

    public EcommerceClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public EcommerceClient setToken(String token) {
        this.token = token;
        return this;
    }

    public String login(String username, String password) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
                .post("/auth/login")
        .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    public Response getProducts() {
        return given()
                .baseUri(baseUrl)
                .header("Authorization", bearer())
        .when()
                .get("/products");
    }

    public Response getProductsRawNoAuth() {
        return given()
                .baseUri(baseUrl)
        .when()
                .get("/products");
    }

    public Response getProductsWithToken(String token) {
        return given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
        .when()
                .get("/products");
    }

    public Response addToCart(int productId, int qty) {
        return given()
                .baseUri(baseUrl)
                .header("Authorization", bearer())
                .contentType("application/json")
                .body("{\"productId\":" + productId + ",\"qty\":" + qty + "}")
        .when()
                .post("/cart/items");
    }

    public Response addToCartNoAuth(int productId, int qty) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body("{\"productId\":" + productId + ",\"qty\":" + qty + "}")
        .when()
                .post("/cart/items");
    }

    public Response createOrder(String cartId) {
        return given()
                .baseUri(baseUrl)
                .header("Authorization", bearer())
                .contentType("application/json")
                .body("{\"cartId\":\"" + cartId + "\"}")
        .when()
                .post("/orders");
    }

    public Response createOrderNoAuth(String cartId) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body("{\"cartId\":\"" + cartId + "\"}")
        .when()
                .post("/orders");
    }

    public Response getOrder(String orderId) {
        return given()
                .baseUri(baseUrl)
                .header("Authorization", bearer())
        .when()
                .get("/orders/" + orderId);
    }

    public Response getOrderNoAuth(String orderId) {
        return given()
                .baseUri(baseUrl)
        .when()
                .get("/orders/" + orderId);
    }

    private String bearer() {
        if (token == null || token.isBlank()) {
            // 让错误尽早暴露：你忘记 setToken 会直接抛异常
            throw new IllegalStateException("token is not set. Call client.setToken(token) first.");
        }
        return "Bearer " + token;
    }
}
