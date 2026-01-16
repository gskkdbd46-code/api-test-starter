package demo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import demo.client.EcommerceClient;
import demo.data.EcommerceRequests;
import demo.stubs.EcommerceStubs;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static demo.assertions.ApiAssertions.*;

@WireMockTest
public class EcommerceApiTest extends TestBase {

    @Test
    @Tag("smoke")
    void smoke_happyPath_login_products_addCart_createOrder_getOrder(WireMockRuntimeInfo wm) {
        EcommerceStubs.register(EcommerceStubs.Scenario.HAPPY_PATH);

        var client = new EcommerceClient(wm.getHttpBaseUrl());

        String token = extractToken(client.login(EcommerceRequests.login("u", "p")));
        client.setToken(token);

        int firstProductId = extractFirstProductId(client.getProducts());
        assertAddToCartOk(client.addToCart(EcommerceRequests.addToCart(firstProductId, 1)));

        String orderId = extractOrderIdCreated(client.createOrder(EcommerceRequests.createOrder("c-1")));
        assertOrderOk(client.getOrder(orderId), orderId);
    }

    @Test
    @Tag("regression")
    void login_should400_whenMissingPassword(WireMockRuntimeInfo wm) {
        EcommerceStubs.register(EcommerceStubs.Scenario.VALIDATION);

        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertBadRequest(client.login(EcommerceRequests.loginMissingPassword("u")));
    }

    @Test
    @Tag("regression")
    void login_should400_whenMissingUsername(WireMockRuntimeInfo wm) {
        EcommerceStubs.register(EcommerceStubs.Scenario.VALIDATION);

        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertBadRequest(client.login(EcommerceRequests.loginMissingUsername("p")));
    }

    @Test
    @Tag("regression")
    void products_should401_withoutToken(WireMockRuntimeInfo wm) {
        demo.stubs.ScenarioPacks.unauthorizedOnly();

        var client = new demo.client.EcommerceClient(wm.getHttpBaseUrl());
        assert401_noAuth("GET /products", client.getProductsNoAuth());
    }



    @Test
    @Tag("regression")
    void products_should401_whenTokenExpired(WireMockRuntimeInfo wm) {
        EcommerceStubs.register(EcommerceStubs.Scenario.TOKEN_EXPIRED);

        var client = new EcommerceClient(wm.getHttpBaseUrl());
        assertTokenExpired(client.getProductsWithToken(EcommerceStubs.EXPIRED_TOKEN));
    }

    @Test
    @Tag("regression")
    void addToCart_should400_whenQtyInvalid(WireMockRuntimeInfo wm) {
        demo.stubs.ScenarioPacks.validationOnly();

        var client = new demo.client.EcommerceClient(wm.getHttpBaseUrl());

        String token = extractToken(client.login(demo.data.EcommerceRequests.login("u", "p")));
        client.setToken(token);

        assertBadRequest(client.addToCart(demo.data.EcommerceRequests.addToCart(101, 0)));
    }


    @Test
    @Tag("regression")
    void addToCart_should401_withoutToken(WireMockRuntimeInfo wm) {
        demo.stubs.ScenarioPacks.unauthorizedOnly();

        var client = new demo.client.EcommerceClient(wm.getHttpBaseUrl());
        assert401_noAuth("POST /cart/items", client.addToCartNoAuth(demo.data.EcommerceRequests.addToCartJson(101, 1)));
    }


    @Test
    @Tag("regression")
    void createOrder_should409_whenOutOfStock(WireMockRuntimeInfo wm) {
        demo.stubs.ScenarioPacks.conflictOnly();

        var client = new demo.client.EcommerceClient(wm.getHttpBaseUrl());

        String token = extractToken(client.login(demo.data.EcommerceRequests.login("u", "p")));
        client.setToken(token);

        assertConflictOutOfStock(client.createOrder(demo.data.EcommerceRequests.createOrder("c-oos")));
    }


    @Test
    @Tag("regression")
    void createOrder_should409_whenDuplicateSubmit(WireMockRuntimeInfo wm) {
        EcommerceStubs.register(EcommerceStubs.Scenario.CONFLICT);

        var client = new EcommerceClient(wm.getHttpBaseUrl());
        String token = extractToken(client.login(EcommerceRequests.login("u", "p")));
        client.setToken(token);

        assertConflictDuplicate(client.createOrder(EcommerceRequests.createOrder("c-dup")));
    }

    @Test
    @Tag("regression")
    void createOrder_should401_withoutToken(WireMockRuntimeInfo wm) {
        demo.stubs.ScenarioPacks.unauthorizedOnly();

        var client = new demo.client.EcommerceClient(wm.getHttpBaseUrl());
        assert401_noAuth("POST /orders", client.createOrderNoAuth(demo.data.EcommerceRequests.createOrderJson("c-1")));
    }


    @Test
    @Tag("regression")
    void getOrder_should404_whenNotFound(WireMockRuntimeInfo wm) {
        demo.stubs.ScenarioPacks.notFoundOnly();

        var client = new demo.client.EcommerceClient(wm.getHttpBaseUrl());

        String token = extractToken(client.login(demo.data.EcommerceRequests.login("u", "p")));
        client.setToken(token);

        assertNotFound(client.getOrder("o-404"));
    }


    @Test
    @Tag("regression")
    void getOrder_should401_withoutToken(WireMockRuntimeInfo wm) {
        demo.stubs.ScenarioPacks.unauthorizedOnly();

        var client = new demo.client.EcommerceClient(wm.getHttpBaseUrl());
        assert401_noAuth("GET /orders/{id}", client.getOrderNoAuth("o-9001"));
    }

    private void assert401_noAuth(String name, io.restassured.response.Response resp) {
        // 这个 helper 只用于“明确不带 token 的用例”
        assertUnauthorized(resp);
    }


}
