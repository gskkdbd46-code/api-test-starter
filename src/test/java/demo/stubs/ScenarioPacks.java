package demo.stubs;

public final class ScenarioPacks {
    private ScenarioPacks() {}

    // 冒烟用：只要 happy path 即可
    public static void smoke() {
        EcommerceStubs.register(EcommerceStubs.Scenario.HAPPY_PATH);
    }

    // 需要登录成功才能继续的回归用例
    public static void withLoginHappy() {
        EcommerceStubs.register(
                EcommerceStubs.Scenario.HAPPY_PATH
        );
    }

    // 只测未授权
    public static void unauthorizedOnly() {
        EcommerceStubs.register(EcommerceStubs.Scenario.UNAUTHORIZED);
    }

    public static void tokenExpiredOnly() {
        EcommerceStubs.register(EcommerceStubs.Scenario.TOKEN_EXPIRED);
    }

    public static void validationOnly() {
        EcommerceStubs.register(EcommerceStubs.Scenario.VALIDATION);
    }

    public static void conflictOnly() {
        EcommerceStubs.register(EcommerceStubs.Scenario.CONFLICT);
    }

    public static void notFoundOnly() {
        EcommerceStubs.register(EcommerceStubs.Scenario.NOT_FOUND);
    }
}
