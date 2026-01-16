package demo.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class EcommerceStubs {

    private EcommerceStubs() {}

    public static final String VALID_TOKEN = "t-123";
    public static final String EXPIRED_TOKEN = "t-expired";

    public enum Scenario {
        // 冒烟主流程：登录成功 -> 查商品 -> 加购 -> 下单 -> 查单
        HAPPY_PATH {
            @Override public void register() {

                // 登录成功（必须有 username + password）
                stubFor(post(urlEqualTo("/auth/login"))
                        .atPriority(1)
                        .withRequestBody(matchingJsonPath("$.username"))
                        .withRequestBody(matchingJsonPath("$.password"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"token\":\"" + VALID_TOKEN + "\"}")));

                // 查商品（任意 Bearer token -> 200）
                stubFor(get(urlEqualTo("/products"))
                        .atPriority(5)
                        .withHeader("Authorization", matching("Bearer .*"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("[{\"id\":101,\"name\":\"Keyboard\",\"price\":199},{\"id\":102,\"name\":\"Mouse\",\"price\":99}]")));

                // 加购（qty=1 -> 200）
                stubFor(post(urlEqualTo("/cart/items"))
                        .atPriority(5)
                        .withHeader("Authorization", matching("Bearer .*"))
                        .withRequestBody(matchingJsonPath("$.productId"))
                        .withRequestBody(matchingJsonPath("$.qty", matching("1")))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"cartId\":\"c-1\",\"itemCount\":1}")));

                // 下单（cartId=c-1 -> 200）
                stubFor(post(urlEqualTo("/orders"))
                        .atPriority(5)
                        .withHeader("Authorization", matching("Bearer .*"))
                        .withRequestBody(matchingJsonPath("$.cartId", equalTo("c-1")))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"orderId\":\"o-9001\",\"status\":\"CREATED\",\"amount\":199}")));

                // 查单（orderId=o-9001 -> 200）
                stubFor(get(urlEqualTo("/orders/o-9001"))
                        .atPriority(5)
                        .withHeader("Authorization", matching("Bearer .*"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"orderId\":\"o-9001\",\"status\":\"CREATED\",\"amount\":199}")));
            }
        },

        // 未授权：不带 Authorization -> 401
        UNAUTHORIZED {
            @Override public void register() {

                stubFor(get(urlEqualTo("/products"))
                        .atPriority(1)
                        .withHeader("Authorization", absent())
                        .willReturn(json(401, "{\"error\":\"unauthorized\"}")));

                stubFor(post(urlEqualTo("/cart/items"))
                        .atPriority(1)
                        .withHeader("Authorization", absent())
                        .willReturn(json(401, "{\"error\":\"unauthorized\"}")));

                stubFor(post(urlEqualTo("/orders"))
                        .atPriority(1)
                        .withHeader("Authorization", absent())
                        .willReturn(json(401, "{\"error\":\"unauthorized\"}")));

                stubFor(get(urlPathMatching("/orders/.*"))
                        .atPriority(1)
                        .withHeader("Authorization", absent())
                        .willReturn(json(401, "{\"error\":\"unauthorized\"}")));
            }
        },

        // token 过期：Bearer t-expired -> 401 token_expired
        TOKEN_EXPIRED {
            @Override public void register() {
                stubFor(get(urlEqualTo("/products"))
                        .atPriority(1)
                        .withHeader("Authorization", equalTo("Bearer " + EXPIRED_TOKEN))
                        .willReturn(json(401, "{\"error\":\"token_expired\"}")));
            }
        },

        // 参数校验：缺字段 / qty 非法 -> 400
        VALIDATION {
            @Override public void register() {

                // 登录成功（给需要 token 的用例用）
                stubFor(post(urlEqualTo("/auth/login"))
                        .atPriority(1)
                        .withRequestBody(matchingJsonPath("$.username"))
                        .withRequestBody(matchingJsonPath("$.password"))
                        .willReturn(json(200, "{\"token\":\"" + VALID_TOKEN + "\"}")));

                // 登录缺参 -> 400（兜底）
                stubFor(post(urlEqualTo("/auth/login"))
                        .atPriority(10)
                        .willReturn(json(400, "{\"error\":\"bad_request\",\"message\":\"missing username or password\"}")));

                // qty=0 或负数 -> 400
                stubFor(post(urlEqualTo("/cart/items"))
                        .atPriority(1)
                        .withHeader("Authorization", matching("Bearer .*"))
                        .withRequestBody(matchingJsonPath("$.qty", matching("0|-[0-9]+")))
                        .willReturn(json(400, "{\"error\":\"bad_request\",\"message\":\"qty must be >= 1\"}")));
            }
        },

        // 冲突：缺货/重复提交 -> 409
        CONFLICT {
            @Override public void register() {

                // 登录成功（给需要 token 的用例用）
                stubFor(post(urlEqualTo("/auth/login"))
                        .atPriority(1)
                        .withRequestBody(matchingJsonPath("$.username"))
                        .withRequestBody(matchingJsonPath("$.password"))
                        .willReturn(json(200, "{\"token\":\"" + VALID_TOKEN + "\"}")));

                stubFor(post(urlEqualTo("/orders"))
                        .atPriority(1)
                        .withHeader("Authorization", matching("Bearer .*"))
                        .withRequestBody(matchingJsonPath("$.cartId", equalTo("c-oos")))
                        .willReturn(json(409, "{\"error\":\"out_of_stock\"}")));

                stubFor(post(urlEqualTo("/orders"))
                        .atPriority(1)
                        .withHeader("Authorization", matching("Bearer .*"))
                        .withRequestBody(matchingJsonPath("$.cartId", equalTo("c-dup")))
                        .willReturn(json(409, "{\"error\":\"duplicate_submit\"}")));
            }
        },

        // 资源不存在：/orders/o-404 -> 404
        NOT_FOUND {
            @Override public void register() {

                // 登录成功（给需要 token 的用例用）
                stubFor(post(urlEqualTo("/auth/login"))
                        .atPriority(1)
                        .withRequestBody(matchingJsonPath("$.username"))
                        .withRequestBody(matchingJsonPath("$.password"))
                        .willReturn(json(200, "{\"token\":\"" + VALID_TOKEN + "\"}")));

                stubFor(get(urlEqualTo("/orders/o-404"))
                        .atPriority(1)
                        .withHeader("Authorization", matching("Bearer .*"))
                        .willReturn(json(404, "{\"error\":\"not_found\"}")));
            }
        };

        public abstract void register();
    }

    public static void register(Scenario... scenarios) {
        for (Scenario s : scenarios) {
            s.register();
        }
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder json(int status, String body) {
        return aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
    }
}
