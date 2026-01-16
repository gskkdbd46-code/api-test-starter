package demo;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.config.LogConfig.logConfig;

public abstract class TestBase {

    private PrintStream logStream;
    private Path logFile;

    @BeforeAll
    static void prepareLogDir() throws IOException {
        Files.createDirectories(Paths.get("target", "restassured-failures"));
    }

    @BeforeEach
    void openFailureLog(TestInfo testInfo) throws IOException {
        String className = testInfo.getTestClass().map(Class::getSimpleName).orElse("UnknownClass");
        String methodName = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknownTest");
        String safeName = (className + "-" + methodName).replaceAll("[^a-zA-Z0-9._-]", "_");

        logFile = Paths.get("target", "restassured-failures", safeName + ".log");
        logStream = new PrintStream(new FileOutputStream(logFile.toFile(), false), true, StandardCharsets.UTF_8);

        RestAssured.config = config().logConfig(
                logConfig()
                        // 只在 then() 校验失败时打印请求+响应
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        // 输出更好读
                        .enablePrettyPrinting(true)
                        // 避免把敏感头打进日志
                        .blacklistHeader("Authorization")
                        // 写入文件
                        .defaultStream(logStream)
        );
    }

    @AfterEach
    void closeFailureLog() throws IOException {
        if (logStream != null) {
            logStream.flush();
            logStream.close();
        }
        // 通过的测试不会触发“失败日志”，文件为空就删掉
        if (logFile != null && Files.exists(logFile) && Files.size(logFile) == 0L) {
            Files.delete(logFile);
        }
    }
}
