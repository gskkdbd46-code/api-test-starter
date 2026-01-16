package demo.data;

public final class EcommerceRequests {
    private EcommerceRequests() {}

    public static String login(String username, String password) {
        return "{\"username\":\"" + esc(username) + "\",\"password\":\"" + esc(password) + "\"}";
    }

    public static String loginMissingPassword(String username) {
        return "{\"username\":\"" + esc(username) + "\"}";
    }

    public static String loginMissingUsername(String password) {
        return "{\"password\":\"" + esc(password) + "\"}";
    }

    public static String addToCart(int productId, int qty) {
        return "{\"productId\":" + productId + ",\"qty\":" + qty + "}";
    }

    public static String createOrder(String cartId) {
        return "{\"cartId\":\"" + esc(cartId) + "\"}";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    public static String addToCartJson(int productId, int qty) {
        return "{\"productId\":" + productId + ",\"qty\":" + qty + "}";
    }

    public static String createOrderJson(String cartId) {
        return "{\"cartId\":\"" + cartId + "\"}";
    }

}
