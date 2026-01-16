[![tests](https://github.com/gskkdbd46-code/api-test-starter/actions/workflows/tests.yml/badge.svg)](https://github.com/gskkdbd46-code/api-test-starter/actions/workflows/tests.yml)



# api-test-starter (E-commerce API Test Demo)

一个最小可交付的接口自动化项目：
- JUnit 5：组织用例 + Tag 分组（smoke / regression）
- RestAssured：发请求 + 断言
- WireMock：模拟后端 API（无真实后端也能跑）
- 失败自动落盘：target/restassured-failures/*.log
- Maven Surefire：按 Tag 选择性运行

## Requirements
- JDK 17+
- Maven 3.9+

## Project Structure
- src/test/java/demo/stubs/EcommerceStubs.java：WireMock stub（模拟电商 API）
- src/test/java/demo/client/EcommerceClient.java：请求封装（Client 层）
- src/test/java/demo/EcommerceApiTest.java：测试用例（业务步骤 + 断言）
- src/test/java/demo/TestBase.java：失败日志配置（落盘）

## Run
全部测试：
```bash
mvn test
```



## Test Suite Map

### Tags
- `smoke`: happy path 冒烟链路（登录 -> 查商品 -> 加购 -> 下单 -> 查单）
- `regression`: 规则/异常回归（401/400/409/404 等）

### Coverage
**Smoke**
- POST `/auth/login` -> 200
- GET `/products` -> 200
- POST `/cart/items` -> 200
- POST `/orders` -> 200
- GET `/orders/{id}` -> 200

**Regression**
- Unauthorized (401): 访问需要鉴权的接口但不带 token
- Validation (400): 请求参数非法（如 qty=0）
- Conflict (409): 业务冲突（如 out-of-stock / duplicate submit）
- Not Found (404): 查询不存在订单

### Run Locally
```bash
mvn test
mvn test -DjunitTags=smoke
mvn test -DjunitTags=regression
