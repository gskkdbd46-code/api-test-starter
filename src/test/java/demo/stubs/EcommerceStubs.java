package demo.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class EcommerceStubs {

    public static final String VALID_TOKEN = "t-123";
    public static final String EXPIRED_TOKEN = "t-expired";

    public static void registerAll() {

        // --------------------
        // 1) 登录：成功 / 缺参
        // --------------------
        stubFor(post(urlEqualTo("/auth/login"))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.username"))
                .withRequestBody(matchingJsonPath("$.password"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\":\"" + VALID_TOKEN + "\"}")));

        // 缺 username 或 password -> 400（兜底）
        stubFor(post(urlEqualTo("/auth/login"))
                .atPriority(10)
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"bad_request\",\"message\":\"missing username or password\"}")));

        // --------------------
        // 2) 商品列表：无 token / 过期 token / 正常
        // --------------------
        stubFor(get(urlEqualTo("/products"))
                .atPriority(1)
                .withHeader("Authorization", absent())
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"unauthorized\"}")));

        stubFor(get(urlEqualTo("/products"))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer " + EXPIRED_TOKEN))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"token_expired\"}")));

        stubFor(get(urlEqualTo("/products"))
                .atPriority(10)
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [{"id":101,"name":"Keyboard","price":199},{"id":102,"name":"Mouse","price":99}]
                        """)));

        // --------------------
        // 3) 加购：无 token / qty 校验 / 正常
        // --------------------
        stubFor(post(urlEqualTo("/cart/items"))
                .atPriority(1)
                .withHeader("Authorization", absent())
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"unauthorized\"}")));

        stubFor(post(urlEqualTo("/cart/items"))
                .atPriority(2)
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.qty", matching("0|-\\d+")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"bad_request\",\"message\":\"qty must be >= 1\"}")));

        stubFor(post(urlEqualTo("/cart/items"))
                .atPriority(10)
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"cartId\":\"c-1\",\"itemCount\":1}")));

        // --------------------
        // 4) 下单：无 token / 库存不足 / 重复提交 / 正常
        // --------------------
        stubFor(post(urlEqualTo("/orders"))
                .atPriority(1)
                .withHeader("Authorization", absent())
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"unauthorized\"}")));

        stubFor(post(urlEqualTo("/orders"))
                .atPriority(2)
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.cartId", equalTo("c-oos")))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"out_of_stock\"}")));

        stubFor(post(urlEqualTo("/orders"))
                .atPriority(3)
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.cartId", equalTo("c-dup")))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"duplicate_submit\"}")));

        stubFor(post(urlEqualTo("/orders"))
                .atPriority(10)
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"orderId\":\"o-9001\",\"status\":\"CREATED\",\"amount\":199}")));

        // --------------------
        // 5) 查单：无 token / 不存在 / 正常
        // --------------------
        stubFor(get(urlPathMatching("/orders/.*"))
                .atPriority(1)
                .withHeader("Authorization", absent())
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"unauthorized\"}")));

        stubFor(get(urlEqualTo("/orders/o-404"))
                .atPriority(2)
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"not_found\"}")));

        stubFor(get(urlPathMatching("/orders/.*"))
                .atPriority(10)
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"orderId\":\"o-9001\",\"status\":\"CREATED\",\"amount\":199}")));
    }

    private EcommerceStubs() {}
}
