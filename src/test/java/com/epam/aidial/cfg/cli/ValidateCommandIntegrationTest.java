package com.epam.aidial.cfg.cli;

import com.epam.aidial.cfg.cli.dto.ValidateResult;
import com.epam.aidial.cfg.cli.dto.ValidationStatus;
import com.epam.aidial.cfg.functional.config.H2FunctionalTestConfiguration;
import com.epam.aidial.cfg.service.config.transfer.MultiFileImportStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {"datasource.vendor=H2", "spring.jpa.show-sql=false"})
@Import({H2FunctionalTestConfiguration.class, ValidateCommand.class})
class ValidateCommandIntegrationTest {

    @Autowired
    private ValidateCommand validateCommand;

    @Autowired
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    private ByteArrayOutputStream stdout;
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void redirectStdout() {
        stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    @Test
    void singleValidFile_returns0_statusValid() throws Exception {
        Path file = copyResource("cli/valid-model.json");
        setFilePaths(file.toString());
        int code = validateCommand.call();

        assertThat(code).isZero();
        ValidateResult result = parseOutput();
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.VALID);
        assertThat(result.getFiles()).hasSize(1);
        assertThat(result.getFiles().get(0).getStatus()).isEqualTo(ValidationStatus.VALID);
    }

    @Test
    void invalidJson_returns1_statusInvalid_withErrorMessage() throws Exception {
        Path file = copyResource("cli/invalid-json.json");
        setFilePaths(file.toString());
        int code = validateCommand.call();

        assertThat(code).isEqualTo(1);
        ValidateResult result = parseOutput();
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.INVALID);
        assertThat(result.getFiles().get(0).getError()).contains("Invalid JSON");
    }

    @Test
    void fileNotFound_returns1_withErrorMessage() throws Exception {
        setFilePaths("/nonexistent/config.json");
        int code = validateCommand.call();

        assertThat(code).isEqualTo(1);
        ValidateResult result = parseOutput();
        assertThat(result.getFiles().get(0).getError()).contains("not found");
    }

    @Test
    void mergeJsonStrategy_twoValidFiles_returns0() throws Exception {
        Path fileA = writeJson(tempDir.resolve("a.json"),
                "{\"models\":{\"model-a\":{\"displayName\":\"A\",\"endpoint\":\"https://endpoint-a/chat/completions\"}}}");
        Path fileB = writeJson(tempDir.resolve("b.json"),
                "{\"models\":{\"model-b\":{\"displayName\":\"B\",\"endpoint\":\"https://endpoint-b/chat/completions\"}}}");
        setFilePaths(fileA.toString(), fileB.toString());
        setStrategy(MultiFileImportStrategy.MERGE_JSON);
        int code = validateCommand.call();

        assertThat(code).isZero();
        assertThat(parseOutput().getStatus()).isEqualTo(ValidationStatus.VALID);
    }

    @Test
    void sequentialStrategy_secondFileSeesFirstFileEntities() throws Exception {
        Path fileA = copyResource("cli/valid-role.json");
        Path fileB = writeJson(tempDir.resolve("model-with-role.json"),
                "{\"models\":{\"gpt-4\":{\"displayName\":\"GPT-4\"," +
                "\"endpoint\":\"https://api/v1/chat/completions\"," +
                "\"userRoles\":[\"admin-role\"]}}}");
        setFilePaths(fileA.toString(), fileB.toString());
        setStrategy(MultiFileImportStrategy.SEQUENTIAL);
        int code = validateCommand.call();

        assertThat(code).isZero();
    }

    // --- helpers ---

    private void setFilePaths(String... paths) {
        validateCommand.filePaths = List.of(paths);
    }

    private void setStrategy(MultiFileImportStrategy strategy) {
        validateCommand.strategy = strategy;
    }

    private Path copyResource(String resource) throws Exception {
        var url = getClass().getClassLoader().getResource(resource);
        Path dest = tempDir.resolve(Path.of(resource).getFileName());
        Files.copy(Path.of(url.toURI()), dest);
        return dest;
    }

    private Path writeJson(Path path, String json) throws Exception {
        Files.writeString(path, json);
        return path;
    }

    private ValidateResult parseOutput() throws Exception {
        String output = stdout.toString();
        int jsonStart = output.indexOf('{');
        assertThat(jsonStart).as("Expected JSON in stdout but got: %s", output).isGreaterThanOrEqualTo(0);
        return objectMapper.readValue(output.substring(jsonStart), ValidateResult.class);
    }
}
