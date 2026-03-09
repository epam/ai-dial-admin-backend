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
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {"datasource.vendor=H2", "spring.jpa.show-sql=false"})
@Import({H2FunctionalTestConfiguration.class, ValidateCommand.class})
class ValidateCommandIntegrationTest {

    private final PrintStream originalOut = System.out;
    @TempDir
    Path tempDir;
    @Autowired
    private ValidateCommand validateCommand;
    @Autowired
    private ObjectMapper objectMapper;
    private ByteArrayOutputStream stdout;

    @BeforeEach
    void setUp() {
        stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        validateCommand.strategy = MultiFileImportStrategy.MERGE_JSON;
        validateCommand.unknownProperties = UnknownPropertiesPolicy.IGNORE;
        validateCommand.coreConfigVersion = "latest";
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
                "{\"models\":{\"gpt-4\":{\"displayName\":\"GPT-4\","
                        + "\"endpoint\":\"https://api/v1/chat/completions\","
                        + "\"userRoles\":[\"admin-role\"]}}}");
        setFilePaths(fileA.toString(), fileB.toString());
        setStrategy(MultiFileImportStrategy.SEQUENTIAL);
        int code = validateCommand.call();

        assertThat(code).isZero();
    }

    @Test
    void unknownPropertiesIgnore_default_fileWithUnknownField_returns0_withWarning() throws Exception {
        Path file = writeJson(tempDir.resolve("unknown.json"),
                "{\"unknownTopLevelField\":\"value\"}");
        setFilePaths(file.toString());
        // unknownProperties defaults to IGNORE
        int code = validateCommand.call();

        assertThat(code).isZero();
        ValidateResult result = parseOutput();
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.VALID);
        assertThat(result.getFiles().get(0).getWarnings()).contains("Unknown property: 'unknownTopLevelField'");
    }

    @Test
    void unknownPropertiesFail_fileWithUnknownField_returns1() throws Exception {
        Path file = writeJson(tempDir.resolve("unknown.json"),
                "{\"unknownTopLevelField\":\"value\"}");
        setFilePaths(file.toString());
        validateCommand.unknownProperties = UnknownPropertiesPolicy.FAIL;
        int code = validateCommand.call();

        assertThat(code).isEqualTo(1);
        assertThat(parseOutput().getStatus()).isEqualTo(ValidationStatus.INVALID);
    }

    @Test
    void coreConfigVersion_latest_validConfig_returns0_noSchemaWarnings() throws Exception {
        Path file = copyResource("cli/valid-model.json");
        setFilePaths(file.toString());
        validateCommand.coreConfigVersion = "latest";

        int code = validateCommand.call();

        assertThat(code).isZero();
        ValidateResult result = parseOutput();
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.VALID);
        // No schema-version warnings for a clean minimal config
        List<String> warnings = result.getFiles().get(0).getWarnings();
        assertThat(warnings == null || warnings.stream().noneMatch(w -> w.contains("not supported by Core version"))).isTrue();
    }

    @Test
    void coreConfigVersion_olderVersion_configHasNewerField_returns0_withSchemaWarning() throws Exception {
        Path file = copyResource("cli/config-with-global-interceptors.json");
        setFilePaths(file.toString());
        validateCommand.coreConfigVersion = "0.37.0";
        validateCommand.unknownProperties = UnknownPropertiesPolicy.IGNORE;

        int code = validateCommand.call();

        assertThat(code).isZero();
        ValidateResult result = parseOutput();
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.VALID);
        assertThat(result.getFiles().get(0).getWarnings())
                .anyMatch(w -> w.contains("globalInterceptors") && w.contains("0.37.0"));
    }

    @Test
    void coreConfigVersion_olderVersion_failMode_configHasNewerField_returns1() throws Exception {
        Path file = copyResource("cli/config-with-global-interceptors.json");
        setFilePaths(file.toString());
        validateCommand.coreConfigVersion = "0.37.0";
        validateCommand.unknownProperties = UnknownPropertiesPolicy.FAIL;

        int code = validateCommand.call();

        assertThat(code).isEqualTo(1);
        ValidateResult result = parseOutput();
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.INVALID);
        assertThat(result.getFiles().get(0).getError())
                .contains("globalInterceptors").contains("0.37.0");
    }

    @Test
    void coreConfigVersion_prereleaseVersion_normalizedAndAccepted() throws Exception {
        Path file = copyResource("cli/config-with-global-interceptors.json");
        setFilePaths(file.toString());
        // 0.37.0-SNAPSHOT should be normalised to 0.37.0 and behave identically
        validateCommand.coreConfigVersion = "0.37.0-SNAPSHOT";
        validateCommand.unknownProperties = UnknownPropertiesPolicy.IGNORE;

        int code = validateCommand.call();

        assertThat(code).isZero();
        assertThat(parseOutput().getFiles().get(0).getWarnings())
                .anyMatch(w -> w.contains("globalInterceptors") && w.contains("0.37.0"));
    }

    @Test
    void coreConfigVersion_futureVersion_resolvesToLatestSchema_returns0() throws Exception {
        // 99.0.0 exceeds all available schemas; VersionedSchemaLoader resolves to latest
        Path file = copyResource("cli/valid-model.json");
        setFilePaths(file.toString());
        validateCommand.coreConfigVersion = "99.0.0";

        int code = validateCommand.call();

        // Should succeed (resolved to latest schema, minimal config is fully valid)
        assertThat(code).isZero();
        assertThat(parseOutput().getStatus()).isEqualTo(ValidationStatus.VALID);
    }

    @Test
    void coreConfigVersion_belowMinimumSchema_returns2() throws Exception {
        Path file = copyResource("cli/valid-model.json");
        setFilePaths(file.toString());
        validateCommand.coreConfigVersion = "0.1.0"; // below minimum available schema (0.23.0)

        int code = validateCommand.call();

        assertThat(code).isEqualTo(2);
    }

    @Test
    void coreConfigVersion_invalidFormat_returns2() throws Exception {
        Path file = copyResource("cli/valid-model.json");
        setFilePaths(file.toString());
        validateCommand.coreConfigVersion = "not-a-version";

        int code = validateCommand.call();

        assertThat(code).isEqualTo(2);
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
