package demo.client;

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

    public Response login(String bodyJson) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(bodyJson)
        .when()
                .post("/auth/login");
    }

    public Response getProducts() {
        return given()
                .baseUri(baseUrl)
                .header("Authorization", bearer())
        .when()
                .get("/products");
    }

    public Response getProductsNoAuth() {
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

    public Response addToCart(String bodyJson) {
        return given()
                .baseUri(baseUrl)
                .header("Authorization", bearer())
                .contentType("application/json")
                .body(bodyJson)
        .when()
                .post("/cart/items");
    }

    public Response addToCartNoAuth(String bodyJson) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(bodyJson)
        .when()
                .post("/cart/items");
    }

    public Response createOrder(String bodyJson) {
        return given()
                .baseUri(baseUrl)
                .header("Authorization", bearer())
                .contentType("application/json")
                .body(bodyJson)
        .when()
                .post("/orders");
    }

    public Response createOrderNoAuth(String bodyJson) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(bodyJson)
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
            throw new IllegalStateException("token is not set. Call client.setToken(token) first.");
        }
        return "Bearer " + token;
    }
}
