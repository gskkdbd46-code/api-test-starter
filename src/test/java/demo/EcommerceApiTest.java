package demo;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import demo.client.EcommerceClient;
import demo.data.EcommerceRequests;
import demo.stubs.EcommerceStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static demo.assertions.ApiAssertions.*;

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

        String token = extractToken(client.login(EcommerceRequests.login("u", "p")));
        client.setToken(token);

        int firstProductId = extractFirstProductId(client.getProducts());

        assertAddToCartOk(client.addToCart(EcommerceRequests.addToCart(firstProductId, 1)));

        String orderId = extractOrderIdCreated(client.createOrder(EcommerceRequests.createOrder("c-1")));

        assertOrderOk(client.getOrder(orderId), "o-9001");
    }

    @Test
    @Tag("regression")
    void login_should400_whenMissingPassword(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertBadRequest(client.login(EcommerceRequests.loginMissingPassword("u")));
    }

    @Test
    @Tag("regression")
    void login_should400_whenMissingUsername(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertBadRequest(client.login(EcommerceRequests.loginMissingUsername("p")));
    }

    @Test
    @Tag("regression")
    void products_should401_withoutToken(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertUnauthorized(client.getProductsNoAuth());
    }

    @Test
    @Tag("regression")
    void products_should401_whenTokenExpired(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertTokenExpired(client.getProductsWithToken(EcommerceStubs.EXPIRED_TOKEN));
    }

    @Test
    @Tag("regression")
    void addToCart_should400_whenQtyInvalid(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = extractToken(client.login(EcommerceRequests.login("u", "p")));
        client.setToken(token);

        assertBadRequest(client.addToCart(EcommerceRequests.addToCart(101, 0)));
    }

    @Test
    @Tag("regression")
    void addToCart_should401_withoutToken(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertUnauthorized(client.addToCartNoAuth(EcommerceRequests.addToCart(101, 1)));
    }

    @Test
    @Tag("regression")
    void createOrder_should409_whenOutOfStock(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = extractToken(client.login(EcommerceRequests.login("u", "p")));
        client.setToken(token);

        assertConflictOutOfStock(client.createOrder(EcommerceRequests.createOrder("c-oos")));
    }

    @Test
    @Tag("regression")
    void createOrder_should409_whenDuplicateSubmit(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = extractToken(client.login(EcommerceRequests.login("u", "p")));
        client.setToken(token);

        assertConflictDuplicate(client.createOrder(EcommerceRequests.createOrder("c-dup")));
    }

    @Test
    @Tag("regression")
    void createOrder_should401_withoutToken(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertUnauthorized(client.createOrderNoAuth(EcommerceRequests.createOrder("c-1")));
    }

    @Test
    @Tag("regression")
    void getOrder_should404_whenNotFound(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = extractToken(client.login(EcommerceRequests.login("u", "p")));
        client.setToken(token);

        assertNotFound(client.getOrder("o-404"));
    }

    @Test
    @Tag("regression")
    void getOrder_should401_withoutToken(WireMockRuntimeInfo wm) {
        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertUnauthorized(client.getOrderNoAuth("o-9001"));
    }
}
