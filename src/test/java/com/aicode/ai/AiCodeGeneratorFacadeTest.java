package com.aicode.ai;

import com.aicode.constant.AppConstant;
import com.aicode.core.AiCodeGeneratorFacade;
import com.aicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AiCodeGeneratorFacadeFileTest {

    /**
     * 流式生成可能比较慢，给 10 分钟
     */
    private static final Duration STREAM_TIMEOUT = Duration.ofMinutes(10);

    /**
     * 每次测试使用不同的 appId，避免上一次生成的文件影响本次断言
     */
    private static final AtomicLong APP_ID_SEQ =
            new AtomicLong(System.currentTimeMillis() * 1000);

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    @DisplayName("HTML 非流式：生成并保存 index.html")
    void htmlNonStream() throws Exception {
        long appId = nextAppId();

        File dir = aiCodeGeneratorFacade.generateAndSaveCode(
                "生成一个简单的登录页面，代码不超过 50 行",
                CodeGenTypeEnum.HTML,
                appId
        );

        assertFileNotBlank(dir.toPath().resolve("index.html"));
    }

    @Test
    @DisplayName("MULTI_FILE 非流式：生成并保存 index.html/style.css/script.js")
    void multiFileNonStream() throws Exception {
        long appId = nextAppId();

        File dir = aiCodeGeneratorFacade.generateAndSaveCode(
                "生成一个简单的登录页面，必须包含 index.html、style.css、script.js 三个文件，代码不超过 100 行",
                CodeGenTypeEnum.MULTI_FILE,
                appId
        );

        assertMultiFile(dir.toPath());
    }

    @Test
    @DisplayName("HTML 流式：生成并保存 index.html")
    void htmlStream() throws Exception {
        long appId = nextAppId();

        Flux<String> flux = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "生成一个简单的个人主页，代码不超过 50 行",
                CodeGenTypeEnum.HTML,
                appId
        );

        assertStreamNotBlank(flux);

        File dir = expectedDir(CodeGenTypeEnum.HTML, appId);
        assertFileNotBlank(dir.toPath().resolve("index.html"));
    }

    @Test
    @DisplayName("MULTI_FILE 流式：生成并保存 index.html/style.css/script.js")
    void multiFileStream() throws Exception {
        long appId = nextAppId();

        Flux<String> flux = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "生成一个简单的登录页面，必须包含 index.html、style.css、script.js 三个文件，代码不超过 100 行",
                CodeGenTypeEnum.MULTI_FILE,
                appId
        );

        assertStreamNotBlank(flux);

        File dir = expectedDir(CodeGenTypeEnum.MULTI_FILE, appId);
        assertMultiFile(dir.toPath());
    }

    @Test
    @DisplayName("VUE_PROJECT 流式：通过 FileWriteTool 生成 Vue 工程")
    void vueProjectStream() throws Exception {
        long appId = nextAppId();

        Flux<String> flux = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "生成一个简单的任务记录 Vue3 项目，使用 Vite + Vue Router，代码不超过 200 行",
                CodeGenTypeEnum.VUE_PROJECT,
                appId
        );

        assertStreamNotBlank(flux);

        // Vue 模式应该由 FileWriteTool 写入 vue_project_<appId>
        Path projectDir = Path.of(AppConstant.CODE_OUTPUT_ROOT_DIR, "vue_project_" + appId);
        assertThat(projectDir)
                .as("Vue 项目目录应该存在: %s", projectDir)
                .exists()
                .isDirectory();

        assertFileNotBlank(projectDir.resolve("package.json"));
        assertFileNotBlank(projectDir.resolve("src/App.vue"));

        // 如果出现这个目录，说明 VUE_PROJECT 分支错误地又走了 MULTI_FILE 的解析保存逻辑
        Path wrongDir = Path.of(AppConstant.CODE_OUTPUT_ROOT_DIR, "multi_file_" + appId);
        assertThat(wrongDir)
                .as("Vue 模式不应该生成 multi_file 目录，请检查 AiCodeGeneratorFacade 的 VUE_PROJECT 分支")
                .doesNotExist();
    }

    private long nextAppId() {
        return APP_ID_SEQ.incrementAndGet();
    }

    private File expectedDir(CodeGenTypeEnum type, long appId) {
        return new File(AppConstant.CODE_OUTPUT_ROOT_DIR, type.getValue() + "_" + appId);
    }

    private void assertStreamNotBlank(Flux<String> flux) {
        List<String> chunks = flux.collectList().block(STREAM_TIMEOUT);
        assertThat(chunks).as("流式结果不能为 null").isNotNull();

        String content = String.join("", chunks);
        assertThat(content).as("AI 返回内容不能为空").isNotBlank();

        System.out.println("AI 返回内容长度: " + content.length());
    }

    private void assertMultiFile(Path dir) throws Exception {
        assertFileNotBlank(dir.resolve("index.html"));
        assertFileNotBlank(dir.resolve("style.css"));
        assertFileNotBlank(dir.resolve("script.js"));
    }

    private void assertFileNotBlank(Path file) throws Exception {
        assertThat(file)
                .as("文件应该存在: %s", file)
                .exists()
                .isRegularFile();

        long size = Files.size(file);
        assertThat(size)
                .as("文件不能为空: %s", file)
                .isGreaterThan(0L);

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertThat(content)
                .as("文件内容不能为空: %s", file)
                .isNotBlank();

        System.out.printf("生成成功: %s (%d bytes)%n", file.toAbsolutePath(), size);
    }
}