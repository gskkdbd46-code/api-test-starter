package demo;

import demo.client.EcommerceClient;
import demo.stubs.EcommerceStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import static org.hamcrest.Matchers.*;

@WireMockTest
public class EcommerceApiTest extends TestBase {

    @BeforeEach
    void setupStubs(WireMockRuntimeInfo wm) {
        EcommerceStubs.registerAll();
    }

    @Test
    @Tag("smoke")
    void smoke_happyPath_login_products_addCart_createOrder_getOrder(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());

        String token = client.login("u", "p");
        client.setToken(token);

        int firstProductId =
                client.getProducts()
                        .then()
                        .statusCode(200)
                        .body("size()", greaterThanOrEqualTo(1))
                        .extract()
                        .path("[0].id");

        client.addToCart(firstProductId, 1)
                .then()
                .statusCode(200)
                .body("cartId", equalTo("c-1"))
                .body("itemCount", equalTo(1));

        String orderId =
                client.createOrder("c-1")
                        .then()
                        .statusCode(200)
                        .body("status", equalTo("CREATED"))
                        .body("amount", equalTo(199))
                        .extract()
                        .path("orderId");

        client.getOrder(orderId)
                .then()
                .statusCode(200)
                .body("orderId", equalTo("o-9001"))
                .body("status", equalTo("CREATED"))
                .body("amount", equalTo(199));
    }

    @Test
    @Tag("regression")
    void login_should400_whenMissingPassword(WireMockRuntimeInfo wm) {
        // 这条不走 client.login，因为它是“异常输入”用例，直接构造请求更清晰
        new EcommerceClient(wm.getHttpBaseUrl()); // 只是强调 client 可用，不是必须

        io.restassured.RestAssured.given()
                .baseUri(wm.getHttpBaseUrl())
                .contentType("application/json")
                .body("{\"username\":\"u\"}")
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400)
                .body("error", equalTo("bad_request"));
    }

    @Test
    @Tag("regression")
    void login_should400_whenMissingUsername(WireMockRuntimeInfo wm) {
        io.restassured.RestAssured.given()
                .baseUri(wm.getHttpBaseUrl())
                .contentType("application/json")
                .body("{\"password\":\"p\"}")
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400)
                .body("error", equalTo("bad_request"));
    }

    @Test
    @Tag("regression")
    void products_should401_withoutToken(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());

        client.getProductsRawNoAuth()
                .then()
                .statusCode(401)
                .body("error", equalTo("unauthorized"));
    }

    @Test
    @Tag("regression")
    void products_should401_whenTokenExpired(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());

        client.getProductsWithToken(EcommerceStubs.EXPIRED_TOKEN)
                .then()
                .statusCode(401)
                .body("error", equalTo("token_expired"));
    }

    @Test
    @Tag("regression")
    void addToCart_should400_whenQtyInvalid(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = client.login("u", "p");
        client.setToken(token);

        client.addToCart(101, 0)
                .then()
                .statusCode(400)
                .body("error", equalTo("bad_request"));
    }

    @Test
    @Tag("regression")
    void addToCart_should401_withoutToken(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());

        client.addToCartNoAuth(101, 1)
                .then()
                .statusCode(401)
                .body("error", equalTo("unauthorized"));
    }

    @Test
    @Tag("regression")
    void createOrder_should409_whenOutOfStock(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = client.login("u", "p");
        client.setToken(token);

        client.createOrder("c-oos")
                .then()
                .statusCode(409)
                .body("error", equalTo("out_of_stock"));
    }

    @Test
    @Tag("regression")
    void createOrder_should409_whenDuplicateSubmit(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = client.login("u", "p");
        client.setToken(token);

        client.createOrder("c-dup")
                .then()
                .statusCode(409)
                .body("error", equalTo("duplicate_submit"));
    }

    @Test
    @Tag("regression")
    void createOrder_should401_withoutToken(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());

        client.createOrderNoAuth("c-1")
                .then()
                .statusCode(401)
                .body("error", equalTo("unauthorized"));
    }

    @Test
    @Tag("regression")
    void getOrder_should404_whenNotFound(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = client.login("u", "p");
        client.setToken(token);

        client.getOrder("o-404")
                .then()
                .statusCode(404)
                .body("error", equalTo("not_found"));
    }

    @Test
    @Tag("regression")
    void getOrder_should401_withoutToken(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());

        client.getOrderNoAuth("o-9001")
                .then()
                .statusCode(401)
                .body("error", equalTo("unauthorized"));
    }
}
