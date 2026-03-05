# DIAL Admin CLI Tool Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a `validate` CLI command to the existing Docker image that fully validates DIAL Core JSON config files using the same import logic as the running service, and extend auto-import-on-bootstrap to support multiple files with configurable merge strategies.

**Architecture:** `Application.main()` detects CLI args and activates a `cli` Spring profile, which switches to `WebApplicationType.NONE` and configures an in-memory H2 DB. Picocli (`picocli-spring-boot-starter`) provides command parsing; the `validate` command calls the real `ConfigImporter` against the ephemeral DB. Multi-file support introduces two strategies: `MERGE_JSON` (raw JSON deep merge → one import) and `SEQUENTIAL` (one import per file, each commit visible to the next).

**Tech Stack:** Java 17, Spring Boot 3.5, picocli 4.7.6 (`picocli-spring-boot-starter`), Jackson, H2 in-memory, existing `ConfigImporter` / `ConfigMerger`.

**Design doc:** `docs/plans/2026-03-05-dial-admin-cli-design.md`

---

## Task 1: Add picocli dependency

**Files:**
- Modify: `build.gradle`

**Step 1: Add dependency**

In the `dependencies` block, after the mapstruct line:

```groovy
implementation 'info.picocli:picocli-spring-boot-starter:4.7.6'
```

**Step 2: Verify build resolves**

```bash
./gradlew dependencies --configuration compileClasspath | grep picocli
```

Expected: `info.picocli:picocli-spring-boot-starter:4.7.6`

**Step 3: Commit**

```bash
git add build.gradle
git commit -m "feat: add picocli-spring-boot-starter dependency for CLI support"
```

---

## Task 2: Add `MultiFileImportStrategy` enum

**Files:**
- Create: `src/main/java/com/epam/aidial/cfg/service/config/transfer/MultiFileImportStrategy.java`

**Step 1: Write the failing test**

Create `src/test/java/com/epam/aidial/cfg/service/config/transfer/MultiFileImportStrategyTest.java`:

```java
package com.epam.aidial.cfg.service.config.transfer;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MultiFileImportStrategyTest {

    @Test
    void shouldHaveMergeJsonAndSequentialValues() {
        assertThat(MultiFileImportStrategy.values())
                .containsExactlyInAnyOrder(
                        MultiFileImportStrategy.MERGE_JSON,
                        MultiFileImportStrategy.SEQUENTIAL);
    }
}
```

**Step 2: Run test to verify it fails**

```bash
./gradlew test --tests "*MultiFileImportStrategyTest" 2>&1 | tail -20
```

Expected: FAIL — class not found

**Step 3: Create the enum**

```java
package com.epam.aidial.cfg.service.config.transfer;

public enum MultiFileImportStrategy {
    MERGE_JSON,
    SEQUENTIAL
}
```

**Step 4: Run test to verify it passes**

```bash
./gradlew test --tests "*MultiFileImportStrategyTest" 2>&1 | tail -10
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/epam/aidial/cfg/service/config/transfer/MultiFileImportStrategy.java \
        src/test/java/com/epam/aidial/cfg/service/config/transfer/MultiFileImportStrategyTest.java
git commit -m "feat: add MultiFileImportStrategy enum (MERGE_JSON, SEQUENTIAL)"
```

---

## Task 3: Add auto-import multi-file properties

**Files:**
- Create: `src/main/java/com/epam/aidial/cfg/configuration/AutoImportOnBootstrapProperties.java`
- Modify: `src/main/resources/application.properties`

**Step 1: Create properties class**

```java
package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.MultiFileImportStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "config.import.autoImportOnBootstrap")
public class AutoImportOnBootstrapProperties {

    private boolean enabled = false;
    private MultiFileImportStrategy strategy = MultiFileImportStrategy.MERGE_JSON;
    private List<String> filePaths = List.of();
    private ConflictResolutionPolicy conflictResolutionPolicy = ConflictResolutionPolicy.OVERRIDE;
}
```

**Step 2: Add new properties to `application.properties`**

After the existing `config.import.autoImportOnBootstrap.enabled` line, add:

```properties
config.import.autoImportOnBootstrap.strategy=${IMPORT_AUTO_BOOTSTRAP_STRATEGY:MERGE_JSON}
config.import.autoImportOnBootstrap.filePaths=${IMPORT_AUTO_BOOTSTRAP_FILE_PATHS:}
config.import.autoImportOnBootstrap.conflictResolutionPolicy=${IMPORT_AUTO_BOOTSTRAP_CONFLICT_RESOLUTION:OVERRIDE}
```

**Step 3: Run existing tests to confirm nothing broken**

```bash
./gradlew test --tests "*CoreConfigAutoImportOnBootstrap*" 2>&1 | tail -15
```

Expected: PASS

**Step 4: Commit**

```bash
git add src/main/java/com/epam/aidial/cfg/configuration/AutoImportOnBootstrapProperties.java \
        src/main/resources/application.properties
git commit -m "feat: add multi-file auto-import properties (strategy, filePaths, conflictResolutionPolicy)"
```

---

## Task 4: Implement `JsonConfigMerger` (raw JSON deep merge for MERGE_JSON strategy)

**Files:**
- Create: `src/main/java/com/epam/aidial/cfg/service/config/transfer/JsonConfigMerger.java`
- Create: `src/test/java/com/epam/aidial/cfg/service/config/transfer/JsonConfigMergerTest.java`

**Step 1: Write failing tests**

```java
package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonConfigMergerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonConfigMerger merger = new JsonConfigMerger(mapper);

    @Test
    void singleFile_returnsParsedConfig() throws Exception {
        Path file = tempFile("{\"models\":{\"gpt-4\":{\"displayName\":\"GPT-4\",\"endpoint\":\"https://api/v1\"}}}");
        Config result = merger.merge(List.of(file.toString()));
        assertThat(result.getModels()).containsKey("gpt-4");
    }

    @Test
    void laterFileWins_onSameModelKey() throws Exception {
        Path file1 = tempFile("{\"models\":{\"gpt-4\":{\"displayName\":\"old\",\"endpoint\":\"https://old\"}}}");
        Path file2 = tempFile("{\"models\":{\"gpt-4\":{\"displayName\":\"new\",\"endpoint\":\"https://new\"}}}");
        Config result = merger.merge(List.of(file1.toString(), file2.toString()));
        assertThat(result.getModels().get("gpt-4").getDisplayName()).isEqualTo("new");
    }

    @Test
    void laterFileAdds_newKeys() throws Exception {
        Path file1 = tempFile("{\"models\":{\"model-a\":{\"displayName\":\"A\",\"endpoint\":\"https://a\"}}}");
        Path file2 = tempFile("{\"models\":{\"model-b\":{\"displayName\":\"B\",\"endpoint\":\"https://b\"}}}");
        Config result = merger.merge(List.of(file1.toString(), file2.toString()));
        assertThat(result.getModels()).containsKeys("model-a", "model-b");
    }

    @Test
    void emptyList_returnsEmptyConfig() {
        Config result = merger.merge(List.of());
        assertThat(result.getModels()).isNullOrEmpty();
    }

    @Test
    void missingFile_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> merger.merge(List.of("/nonexistent/path.json")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("/nonexistent/path.json");
    }

    @Test
    void invalidJson_throwsIllegalArgumentException() throws Exception {
        Path file = tempFile("{not valid json}");
        assertThatThrownBy(() -> merger.merge(List.of(file.toString())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(file.toString());
    }

    private Path tempFile(String json) throws IOException {
        Path f = Files.createTempFile("test-config", ".json");
        Files.writeString(f, json);
        f.toFile().deleteOnExit();
        return f;
    }
}
```

**Step 2: Run to verify they fail**

```bash
./gradlew test --tests "*JsonConfigMergerTest" 2>&1 | tail -20
```

Expected: FAIL — class not found

**Step 3: Implement `JsonConfigMerger`**

```java
package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonConfigMerger {

    private final ObjectMapper objectMapper;

    public Config merge(List<String> filePaths) {
        if (filePaths.isEmpty()) {
            return new Config();
        }

        JsonNode merged = null;
        for (String path : filePaths) {
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("Config file not found: " + path);
            }
            try {
                JsonNode node = objectMapper.readTree(file);
                merged = (merged == null) ? node : deepMerge(merged, node);
            } catch (JsonParseException e) {
                throw new IllegalArgumentException(
                        "Invalid JSON in file '" + path + "': " + e.getOriginalMessage(), e);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot read file: " + path, e);
            }
        }

        try {
            return objectMapper.treeToValue(merged, Config.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Merged config cannot be deserialized: " + e.getMessage(), e);
        }
    }

    private JsonNode deepMerge(JsonNode base, JsonNode overlay) {
        if (!overlay.isObject() || !base.isObject()) {
            return overlay;
        }
        ObjectNode result = (ObjectNode) base.deepCopy();
        Iterator<Map.Entry<String, JsonNode>> fields = overlay.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode overlayValue = entry.getValue();
            JsonNode baseValue = result.get(key);
            if (baseValue != null && baseValue.isObject() && overlayValue.isObject()) {
                result.set(key, deepMerge(baseValue, overlayValue));
            } else {
                result.set(key, overlayValue);
            }
        }
        return result;
    }
}
```

**Step 4: Run tests to verify they pass**

```bash
./gradlew test --tests "*JsonConfigMergerTest" 2>&1 | tail -10
```

Expected: all PASS

**Step 5: Commit**

```bash
git add src/main/java/com/epam/aidial/cfg/service/config/transfer/JsonConfigMerger.java \
        src/test/java/com/epam/aidial/cfg/service/config/transfer/JsonConfigMergerTest.java
git commit -m "feat: add JsonConfigMerger for MERGE_JSON strategy (deep JSON merge, later file wins)"
```

---

## Task 5: Refactor `CoreConfigAutoImportOnBootstrapService` for multi-file support

**Files:**
- Modify: `src/main/java/com/epam/aidial/cfg/service/config/transfer/CoreConfigAutoImportOnBootstrapService.java`
- Create: `src/test/java/com/epam/aidial/cfg/service/config/transfer/CoreConfigAutoImportOnBootstrapServiceTest.java`

**Step 1: Write failing tests**

```java
package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.cfg.configuration.AutoImportOnBootstrapProperties;
import com.epam.aidial.cfg.domain.service.DatabaseService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoreConfigAutoImportOnBootstrapServiceTest {

    @Mock
    private DatabaseService databaseService;
    @Mock
    private CoreConfigRetriever coreConfigRetriever;
    @Mock
    private ConfigImporter configImporter;
    @Mock
    private CoreConfigAutoImportOnBootstrapLock lock;
    @Mock
    private JsonConfigMerger jsonConfigMerger;
    @Mock
    private AutoImportOnBootstrapProperties properties;
    @InjectMocks
    private CoreConfigAutoImportOnBootstrapService service;

    @Test
    void whenFilePathsEmpty_usesRetriever() {
        when(databaseService.isInitializedEmptyDatabase()).thenReturn(true);
        when(properties.getFilePaths()).thenReturn(List.of());
        Config config = new Config();
        when(coreConfigRetriever.getConfig(true)).thenReturn(config);

        service.autoImportCoreConfig();

        verify(coreConfigRetriever).getConfig(true);
        verify(configImporter).importConfigWithOverride(config);
        verify(jsonConfigMerger, never()).merge(any());
    }

    @Test
    void whenFilePathsConfigured_mergeJson_usesMerger() {
        when(databaseService.isInitializedEmptyDatabase()).thenReturn(true);
        List<String> paths = List.of("/data/a.json", "/data/b.json");
        when(properties.getFilePaths()).thenReturn(paths);
        when(properties.getStrategy()).thenReturn(MultiFileImportStrategy.MERGE_JSON);
        Config merged = new Config();
        when(jsonConfigMerger.merge(paths)).thenReturn(merged);

        service.autoImportCoreConfig();

        verify(jsonConfigMerger).merge(paths);
        verify(configImporter).importConfigWithOverride(merged);
        verify(coreConfigRetriever, never()).getConfig(anyBoolean());
    }

    @Test
    void whenFilePathsConfigured_sequential_importsEachFile() {
        when(databaseService.isInitializedEmptyDatabase()).thenReturn(true);
        List<String> paths = List.of("/data/a.json", "/data/b.json");
        when(properties.getFilePaths()).thenReturn(paths);
        when(properties.getStrategy()).thenReturn(MultiFileImportStrategy.SEQUENTIAL);
        when(properties.getConflictResolutionPolicy()).thenReturn(ConflictResolutionPolicy.OVERRIDE);
        Config configA = new Config();
        Config configB = new Config();
        when(jsonConfigMerger.merge(List.of("/data/a.json"))).thenReturn(configA);
        when(jsonConfigMerger.merge(List.of("/data/b.json"))).thenReturn(configB);

        service.autoImportCoreConfig();

        verify(configImporter).importConfig(eq(configA), any(ConfigImportOptions.class));
        verify(configImporter).importConfig(eq(configB), any(ConfigImportOptions.class));
    }

    @Test
    void whenDatabaseNotEmpty_skipsImport() {
        when(databaseService.isInitializedEmptyDatabase()).thenReturn(false);

        service.autoImportCoreConfig();

        verify(configImporter, never()).importConfigWithOverride(any());
        verify(configImporter, never()).importConfig(any(), any());
    }
}
```

**Step 2: Run to verify they fail**

```bash
./gradlew test --tests "*CoreConfigAutoImportOnBootstrapServiceTest" 2>&1 | tail -20
```

Expected: FAIL

**Step 3: Refactor `CoreConfigAutoImportOnBootstrapService`**

Replace the existing class body (keep package, imports, class declaration, annotations):

```java
package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.cfg.configuration.AutoImportOnBootstrapProperties;
import com.epam.aidial.cfg.domain.service.DatabaseService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "config.import.autoImportOnBootstrap.enabled", havingValue = "true")
public class CoreConfigAutoImportOnBootstrapService {

    private final DatabaseService databaseService;
    private final CoreConfigRetriever coreConfigRetriever;
    private final ConfigImporter configImporter;
    private final CoreConfigAutoImportOnBootstrapLock coreConfigAutoImportOnBootstrapLock;
    private final JsonConfigMerger jsonConfigMerger;
    private final AutoImportOnBootstrapProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void autoImportCoreConfig() {
        try {
            if (databaseService.isInitializedEmptyDatabase()) {
                log.info("Auto import of core config started");
                doImport();
                log.info("Auto import of core config finished");
            } else {
                log.info("Database is not empty. Skipping auto import of core config");
            }
            coreConfigAutoImportOnBootstrapLock.finishAutoImport();
        } catch (Exception exception) {
            log.error("Auto import of core config failed", exception);
            throw exception;
        }
    }

    private void doImport() {
        List<String> filePaths = properties.getFilePaths();
        if (filePaths.isEmpty()) {
            Config config = coreConfigRetriever.getConfig(true);
            configImporter.importConfigWithOverride(config);
            return;
        }

        if (properties.getStrategy() == MultiFileImportStrategy.MERGE_JSON) {
            Config merged = jsonConfigMerger.merge(filePaths);
            configImporter.importConfigWithOverride(merged);
        } else {
            var importOptions = new ConfigImportOptions(properties.getConflictResolutionPolicy());
            for (String path : filePaths) {
                log.info("Sequential import of config file: {}", path);
                Config config = jsonConfigMerger.merge(List.of(path));
                configImporter.importConfig(config, importOptions);
            }
        }
    }
}
```

**Step 4: Run tests**

```bash
./gradlew test --tests "*CoreConfigAutoImportOnBootstrapServiceTest" --tests "*CoreConfigAutoImportOnBootstrap*" 2>&1 | tail -15
```

Expected: all PASS

**Step 5: Commit**

```bash
git add src/main/java/com/epam/aidial/cfg/service/config/transfer/CoreConfigAutoImportOnBootstrapService.java \
        src/main/java/com/epam/aidial/cfg/configuration/AutoImportOnBootstrapProperties.java \
        src/test/java/com/epam/aidial/cfg/service/config/transfer/CoreConfigAutoImportOnBootstrapServiceTest.java
git commit -m "feat: support multi-file auto-import with MERGE_JSON and SEQUENTIAL strategies"
```

---

## Task 6: Add multi-file functional tests for auto-import

**Files:**
- Modify: `src/test/java/com/epam/aidial/cfg/functional/tests/CoreConfigAutoImportOnBootstrapFunctionalTest.java`
- Create: `src/test/resources/import/multifile/config-a.json`
- Create: `src/test/resources/import/multifile/config-b.json`

**Step 1: Create test fixture files**

`src/test/resources/import/multifile/config-a.json`:
```json
{
  "models": {
    "model-a": {
      "displayName": "Model A",
      "endpoint": "https://endpoint-a/chat/completions"
    },
    "model-shared": {
      "displayName": "Shared from A",
      "endpoint": "https://endpoint-shared-a/chat/completions"
    }
  }
}
```

`src/test/resources/import/multifile/config-b.json`:
```json
{
  "models": {
    "model-b": {
      "displayName": "Model B",
      "endpoint": "https://endpoint-b/chat/completions"
    },
    "model-shared": {
      "displayName": "Shared from B (wins)",
      "endpoint": "https://endpoint-shared-b/chat/completions"
    }
  }
}
```

**Step 2: Add tests to `CoreConfigAutoImportOnBootstrapFunctionalTest`**

Add these test methods (the class already has `modelFacade` injected):

```java
@Test
@TestPropertySource(properties = {
        "config.import.autoImportOnBootstrap.strategy=MERGE_JSON",
        "config.import.autoImportOnBootstrap.filePaths=src/test/resources/import/multifile/config-a.json,src/test/resources/import/multifile/config-b.json"
})
public void testMergeJsonStrategy_laterFileWins() {
    Collection<ModelDto> models = modelFacade.getAll();
    assertThat(models).extracting(ModelDto::getName)
            .containsExactlyInAnyOrder("model-a", "model-b", "model-shared");
    assertThat(models).filteredOn(m -> "model-shared".equals(m.getName()))
            .first()
            .extracting(ModelDto::getDisplayName)
            .isEqualTo("Shared from B (wins)");
}

@Test
@TestPropertySource(properties = {
        "config.import.autoImportOnBootstrap.strategy=SEQUENTIAL",
        "config.import.autoImportOnBootstrap.filePaths=src/test/resources/import/multifile/config-a.json,src/test/resources/import/multifile/config-b.json",
        "config.import.autoImportOnBootstrap.conflictResolutionPolicy=OVERRIDE"
})
public void testSequentialStrategy_bothFilesImported() {
    Collection<ModelDto> models = modelFacade.getAll();
    assertThat(models).extracting(ModelDto::getName)
            .containsExactlyInAnyOrder("model-a", "model-b", "model-shared");
}
```

**Note:** `@TestPropertySource` on individual methods is not supported by Spring. Instead, create two new abstract test subclasses per strategy, each with `@TestPropertySource` at class level, and nest them in `H2FunctionalTests`. See the existing `CoreConfigAutoImportOnBootstrapFunctionalTest` pattern.

Concrete approach — add to `CoreConfigAutoImportOnBootstrapFunctionalTest`:

```java
@TestPropertySource(properties = {
        "config.import.autoImportOnBootstrap.enabled=true",
        "config.import.autoImportOnBootstrap.strategy=MERGE_JSON",
        "config.import.autoImportOnBootstrap.filePaths=" +
                "src/test/resources/import/multifile/config-a.json," +
                "src/test/resources/import/multifile/config-b.json"
})
public abstract static class MergeJsonTests {

    @Autowired
    private ModelFacade modelFacade;

    @Test
    public void testMergeJson_laterFileWins() {
        Collection<ModelDto> models = modelFacade.getAll();
        assertThat(models).extracting(ModelDto::getName)
                .containsExactlyInAnyOrder("model-a", "model-b", "model-shared");
        assertThat(models).filteredOn(m -> "model-shared".equals(m.getName()))
                .first().extracting(ModelDto::getDisplayName)
                .isEqualTo("Shared from B (wins)");
    }
}

@TestPropertySource(properties = {
        "config.import.autoImportOnBootstrap.enabled=true",
        "config.import.autoImportOnBootstrap.strategy=SEQUENTIAL",
        "config.import.autoImportOnBootstrap.conflictResolutionPolicy=OVERRIDE",
        "config.import.autoImportOnBootstrap.filePaths=" +
                "src/test/resources/import/multifile/config-a.json," +
                "src/test/resources/import/multifile/config-b.json"
})
public abstract static class SequentialTests {

    @Autowired
    private ModelFacade modelFacade;

    @Test
    public void testSequential_bothFilesImported() {
        Collection<ModelDto> models = modelFacade.getAll();
        assertThat(models).extracting(ModelDto::getName)
                .containsExactlyInAnyOrder("model-a", "model-b", "model-shared");
    }
}
```

Then add nested classes in `H2FunctionalTests`:

```java
@Nested
class CoreConfigAutoImportMergeJsonTests extends CoreConfigAutoImportOnBootstrapFunctionalTest.MergeJsonTests {
}

@Nested
class CoreConfigAutoImportSequentialTests extends CoreConfigAutoImportOnBootstrapFunctionalTest.SequentialTests {
}
```

**Step 3: Run new tests**

```bash
./gradlew test --tests "*H2FunctionalTests*CoreConfigAutoImport*" 2>&1 | tail -20
```

Expected: all PASS

**Step 4: Commit**

```bash
git add src/test/java/com/epam/aidial/cfg/functional/tests/CoreConfigAutoImportOnBootstrapFunctionalTest.java \
        src/test/java/com/epam/aidial/cfg/functional/H2FunctionalTests.java \
        src/test/resources/import/multifile/
git commit -m "test: add multi-file auto-import functional tests (MERGE_JSON and SEQUENTIAL)"
```

---

## Task 7: Add `application-cli.properties` (CLI Spring profile)

**Files:**
- Create: `src/main/resources/application-cli.properties`

**Step 1: Create the file**

```properties
# CLI mode: no web server, ephemeral in-memory H2, all background services disabled

# Datasource - always H2 in-memory for CLI (ephemeral, no credentials needed)
datasource.vendor=H2
h2.datasource.url=jdbc:h2:mem:cli-validation;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
h2.datasource.username=sa
h2.datasource.password=
h2.datasource.masterKey=
h2.datasource.encryptedFileKey=

# Disable config export scheduler
config.export.enabled=false
config.reload.enabled=false
config.autoReload.enabled=false

# Disable auto-import (CLI validate command handles this directly)
config.import.autoImportOnBootstrap.enabled=false

# Disable security (no HTTP endpoints in CLI mode)
config.rest.security.mode=none

# Disable actuator and metrics
management.endpoints.web.exposure.include=
metrics.enabled=false

# Suppress unnecessary startup noise
spring.jpa.show-sql=false
logging.level.org.flywaydb=WARN
logging.level.org.hibernate=WARN
logging.level.org.springframework=WARN
logging.level.com.epam.aidial.cfg=WARN
```

**Step 2: No test needed for this step (verified implicitly by CLI integration test in Task 11)**

**Step 3: Commit**

```bash
git add src/main/resources/application-cli.properties
git commit -m "feat: add application-cli.properties for CLI mode (in-memory H2, no web server)"
```

---

## Task 8: Modify `Application.main()` for CLI mode detection

**Files:**
- Modify: `src/main/java/com/epam/aidial/cfg/Application.java`

The goal: if the first argument is a known CLI command, activate `cli` profile and disable web server. Skip `DatasourceVendorValidator` in this case (it's configured by `application-cli.properties`).

**Step 1: Modify `Application.java`**

```java
package com.epam.aidial.cfg;

import com.epam.aidial.cfg.configuration.DatasourceVendorValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Set;

@SpringBootApplication
@EnableAspectJAutoProxy
public class Application {

    private static final Set<String> CLI_COMMANDS = Set.of("validate");

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        if (args.length > 0 && CLI_COMMANDS.contains(args[0])) {
            application.setWebApplicationType(WebApplicationType.NONE);
            application.setAdditionalProfiles("cli");
        } else {
            application.addListeners(new DatasourceVendorValidator());
        }
        application.run(args);
    }
}
```

**Step 2: Run existing tests to make sure nothing broken**

```bash
./gradlew test --tests "*H2FunctionalTests*" 2>&1 | tail -15
```

Expected: all PASS

**Step 3: Commit**

```bash
git add src/main/java/com/epam/aidial/cfg/Application.java
git commit -m "feat: detect CLI commands in main() and activate cli Spring profile"
```

---

## Task 9: Create `DialAdminCommand` and `CliApplicationRunner`

**Files:**
- Create: `src/main/java/com/epam/aidial/cfg/cli/DialAdminCommand.java`
- Create: `src/main/java/com/epam/aidial/cfg/cli/CliApplicationRunner.java`

**Step 1: Create `DialAdminCommand`**

```java
package com.epam.aidial.cfg.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
        name = "dial-admin",
        mixinStandardHelpOptions = true,
        description = "DIAL Admin CLI tool",
        subcommands = {ValidateCommand.class}
)
public class DialAdminCommand implements Runnable {

    @Override
    public void run() {
        // no subcommand given — print usage (handled by picocli)
    }
}
```

**Step 2: Create `CliApplicationRunner`**

```java
package com.epam.aidial.cfg.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
@Profile("cli")
@RequiredArgsConstructor
public class CliApplicationRunner implements ApplicationRunner {

    private final DialAdminCommand dialAdminCommand;
    private final IFactory factory;

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = new CommandLine(dialAdminCommand, factory)
                .execute(args.getSourceArgs());
        System.exit(exitCode);
    }
}
```

**Step 3: Verify compilation**

```bash
./gradlew compileJava 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL (no compile errors; `ValidateCommand` will be created in Task 10)

**Note:** Compilation will fail until `ValidateCommand` exists. Move to Task 10 immediately.

**Step 4: Commit (after Task 10 passes compilation)**

```bash
git add src/main/java/com/epam/aidial/cfg/cli/DialAdminCommand.java \
        src/main/java/com/epam/aidial/cfg/cli/CliApplicationRunner.java
git commit -m "feat: add DialAdminCommand root picocli command and CliApplicationRunner"
```

---

## Task 10: Create `ValidateCommand` with output DTOs

**Files:**
- Create: `src/main/java/com/epam/aidial/cfg/cli/dto/FileValidationResult.java`
- Create: `src/main/java/com/epam/aidial/cfg/cli/dto/ValidateResult.java`
- Create: `src/main/java/com/epam/aidial/cfg/cli/ValidateCommand.java`

**Step 1: Create DTOs**

`FileValidationResult.java`:
```java
package com.epam.aidial.cfg.cli.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileValidationResult {
    String path;
    String status;
    String error;
}
```

`ValidateResult.java`:
```java
package com.epam.aidial.cfg.cli.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ValidateResult {
    String status;
    String strategy;
    List<FileValidationResult> files;
}
```

**Step 2: Create `ValidateCommand`**

```java
package com.epam.aidial.cfg.cli;

import com.epam.aidial.cfg.cli.dto.FileValidationResult;
import com.epam.aidial.cfg.cli.dto.ValidateResult;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.JsonConfigMerger;
import com.epam.aidial.cfg.service.config.transfer.MultiFileImportStrategy;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Component
@Command(
        name = "validate",
        mixinStandardHelpOptions = true,
        description = "Validate one or more DIAL Core JSON config files"
)
@Slf4j
@RequiredArgsConstructor
public class ValidateCommand implements Callable<Integer> {

    @Parameters(arity = "1..*", description = "Paths to DIAL Core JSON config files to validate")
    private List<String> filePaths;

    @Option(names = "--strategy", defaultValue = "MERGE_JSON",
            description = "Multi-file strategy: MERGE_JSON (default) or SEQUENTIAL")
    private MultiFileImportStrategy strategy;

    @Option(names = "--conflict-resolution", defaultValue = "OVERRIDE",
            description = "Conflict resolution for SEQUENTIAL strategy: OVERRIDE (default) or SKIP")
    private ConflictResolutionPolicy conflictResolution;

    private final ConfigImporter configImporter;
    private final JsonConfigMerger jsonConfigMerger;
    private final ObjectMapper objectMapper;

    @Override
    public Integer call() {
        List<FileValidationResult> results = new ArrayList<>();
        boolean anyInvalid = false;

        try {
            if (strategy == MultiFileImportStrategy.MERGE_JSON) {
                FileValidationResult result = validateMerged(filePaths);
                results.add(result);
                if ("invalid".equals(result.getStatus())) {
                    anyInvalid = true;
                }
            } else {
                for (String path : filePaths) {
                    FileValidationResult result = validateSingle(path);
                    results.add(result);
                    if ("invalid".equals(result.getStatus())) {
                        anyInvalid = true;
                        break; // stop on first failure in sequential mode
                    }
                }
            }

            ValidateResult output = ValidateResult.builder()
                    .status(anyInvalid ? "invalid" : "valid")
                    .strategy(strategy.name())
                    .files(results)
                    .build();
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(output));
            return anyInvalid ? 1 : 0;

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            log.error("Unexpected CLI error", e);
            return 2;
        }
    }

    private FileValidationResult validateMerged(List<String> paths) {
        String displayPath = paths.size() == 1 ? paths.get(0) : paths.toString();
        try {
            var config = jsonConfigMerger.merge(paths);
            configImporter.importConfigWithOverride(config);
            return FileValidationResult.builder().path(displayPath).status("valid").build();
        } catch (IllegalArgumentException e) {
            return FileValidationResult.builder().path(displayPath).status("invalid")
                    .error(e.getMessage()).build();
        } catch (Exception e) {
            return FileValidationResult.builder().path(displayPath).status("invalid")
                    .error(toErrorMessage(e)).build();
        }
    }

    private FileValidationResult validateSingle(String path) {
        try {
            var config = jsonConfigMerger.merge(List.of(path));
            configImporter.importConfig(config, new ConfigImportOptions(conflictResolution));
            return FileValidationResult.builder().path(path).status("valid").build();
        } catch (IllegalArgumentException e) {
            return FileValidationResult.builder().path(path).status("invalid")
                    .error(e.getMessage()).build();
        } catch (Exception e) {
            return FileValidationResult.builder().path(path).status("invalid")
                    .error(toErrorMessage(e)).build();
        }
    }

    private String toErrorMessage(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof JsonParseException jpe) {
            return "Invalid JSON at line " + jpe.getLocation().getLineNr() + ": " + jpe.getOriginalMessage();
        }
        if (cause instanceof JsonMappingException jme) {
            return "Invalid field '" + jme.getPathReference() + "': " + jme.getOriginalMessage();
        }
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
```

**Step 3: Verify compilation**

```bash
./gradlew compileJava 2>&1 | tail -15
```

Expected: BUILD SUCCESSFUL

**Step 4: Run all tests to confirm nothing broken**

```bash
./gradlew test 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add src/main/java/com/epam/aidial/cfg/cli/
git commit -m "feat: add ValidateCommand with MERGE_JSON and SEQUENTIAL strategies and JSON output"
```

---

## Task 11: Integration tests for `ValidateCommand`

**Files:**
- Create: `src/test/java/com/epam/aidial/cfg/cli/ValidateCommandIntegrationTest.java`
- Create: `src/test/resources/cli/valid-model.json`
- Create: `src/test/resources/cli/invalid-json.json`
- Create: `src/test/resources/cli/duplicate-model.json`

**Step 1: Create test fixture files**

`src/test/resources/cli/valid-model.json`:
```json
{
  "models": {
    "gpt-4": {
      "displayName": "GPT-4",
      "endpoint": "https://api.openai.com/v1/chat/completions"
    }
  }
}
```

`src/test/resources/cli/invalid-json.json`:
```
{not valid json at all
```

`src/test/resources/cli/valid-role.json`:
```json
{
  "roles": {
    "admin-role": {
      "limits": {}
    }
  }
}
```

**Step 2: Write integration tests**

```java
package com.epam.aidial.cfg.cli;

import com.epam.aidial.cfg.cli.dto.ValidateResult;
import com.epam.aidial.cfg.functional.config.H2FunctionalTestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {"datasource.vendor=H2"})
@Import(H2FunctionalTestConfiguration.class)
class ValidateCommandIntegrationTest {

    @Autowired
    private ValidateCommand validateCommand;

    @Autowired
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    private ByteArrayOutputStream stdout;

    @BeforeEach
    void redirectStdout() {
        stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
    }

    @Test
    void singleValidFile_returns0_statusValid() throws Exception {
        Path file = copyResource("cli/valid-model.json");
        setFilePaths(file.toString());
        int code = validateCommand.call();

        assertThat(code).isZero();
        ValidateResult result = parseOutput();
        assertThat(result.getStatus()).isEqualTo("valid");
        assertThat(result.getFiles()).hasSize(1);
        assertThat(result.getFiles().get(0).getStatus()).isEqualTo("valid");
    }

    @Test
    void invalidJson_returns1_statusInvalid_withLineNumber() throws Exception {
        Path file = copyResource("cli/invalid-json.json");
        setFilePaths(file.toString());
        int code = validateCommand.call();

        assertThat(code).isEqualTo(1);
        ValidateResult result = parseOutput();
        assertThat(result.getStatus()).isEqualTo("invalid");
        assertThat(result.getFiles().get(0).getError()).contains("Invalid JSON at line");
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
                "{\"models\":{\"model-a\":{\"displayName\":\"A\",\"endpoint\":\"https://a\"}}}");
        Path fileB = writeJson(tempDir.resolve("b.json"),
                "{\"models\":{\"model-b\":{\"displayName\":\"B\",\"endpoint\":\"https://b\"}}}");
        setFilePaths(fileA.toString(), fileB.toString());
        setStrategy(MultiFileImportStrategy.MERGE_JSON);
        int code = validateCommand.call();

        assertThat(code).isZero();
        assertThat(parseOutput().getStatus()).isEqualTo("valid");
    }

    @Test
    void sequentialStrategy_secondFileSeesFirstFileEntities() throws Exception {
        // role defined in file 1, model referencing that role in file 2
        Path fileA = copyResource("cli/valid-role.json");
        Path fileB = writeJson(tempDir.resolve("model-with-role.json"),
                "{\"models\":{\"gpt-4\":{\"displayName\":\"GPT-4\"," +
                "\"endpoint\":\"https://api/v1\"," +
                "\"userRoles\":[\"admin-role\"]}}}");
        setFilePaths(fileA.toString(), fileB.toString());
        setStrategy(MultiFileImportStrategy.SEQUENTIAL);
        int code = validateCommand.call();

        assertThat(code).isZero();
    }

    // helper methods

    private void setFilePaths(String... paths) throws Exception {
        var field = ValidateCommand.class.getDeclaredField("filePaths");
        field.setAccessible(true);
        field.set(validateCommand, java.util.List.of(paths));
    }

    private void setStrategy(MultiFileImportStrategy strategy) throws Exception {
        var field = ValidateCommand.class.getDeclaredField("strategy");
        field.setAccessible(true);
        field.set(validateCommand, strategy);
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
        System.setOut(System.out);
        return objectMapper.readValue(stdout.toString(), ValidateResult.class);
    }
}
```

**Step 3: Run integration tests**

```bash
./gradlew test --tests "*ValidateCommandIntegrationTest" 2>&1 | tail -25
```

Expected: all PASS

**Step 4: Run full test suite**

```bash
./gradlew test 2>&1 | tail -15
```

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add src/test/java/com/epam/aidial/cfg/cli/ \
        src/test/resources/cli/
git commit -m "test: add ValidateCommand integration tests for all scenarios"
```

---

## Task 12: Write `docs/cli.md` usage guide

**Files:**
- Create: `docs/cli.md`

**Step 1: Create the usage guide**

```markdown
# DIAL Admin CLI

The DIAL Admin CLI is built into the `epam/ai-dial-admin-backend` Docker image. No separate installation is needed.

## Prerequisites

- Docker
- Config files accessible via a mounted volume

## Commands

### `validate`

Validates one or more DIAL Core JSON config files using the same import logic as the running service.

**Usage:**
```
docker run --rm \
  -v /path/to/your/configs:/data \
  epam/ai-dial-admin-backend:<tag> \
  validate [--strategy <MERGE_JSON|SEQUENTIAL>] [--conflict-resolution <OVERRIDE|SKIP>] <file1> [file2 ...]
```

**Options:**

| Option | Default | Description |
|--------|---------|-------------|
| `--strategy` | `MERGE_JSON` | How multiple files are combined before validation |
| `--conflict-resolution` | `OVERRIDE` | Conflict policy used for `SEQUENTIAL` strategy |
| `--help` | | Show command help |

**Strategies:**

- **`MERGE_JSON`** *(default)*: All files are deep-merged as JSON (later files win on conflicting keys), then validated as a single unit. Use this when your config is split across files that together form one complete configuration.

- **`SEQUENTIAL`**: Each file is imported independently in order. Later imports see entities created by earlier ones. Use this when files build on each other (e.g., `file2` references a role defined in `file1`).

## Exit Codes

| Code | Meaning |
|------|---------|
| `0` | All files are valid |
| `1` | One or more files are invalid (JSON output on stdout with details) |
| `2` | Unexpected error (e.g., file not found) — message on stderr |

## Output Format

Output is always valid JSON on stdout.

**Success:**
```json
{
  "status": "valid",
  "strategy": "MERGE_JSON",
  "files": [
    { "path": "/data/core.json", "status": "valid" },
    { "path": "/data/extra.json", "status": "valid" }
  ]
}
```

**Failure:**
```json
{
  "status": "invalid",
  "strategy": "SEQUENTIAL",
  "files": [
    { "path": "/data/core.json", "status": "valid" },
    {
      "path": "/data/extra.json",
      "status": "invalid",
      "error": "Model 'gpt-4': upstream key 'prod-key' does not exist"
    }
  ]
}
```

## Examples

**Validate a single file:**
```bash
docker run --rm -v $(pwd)/configs:/data \
  epam/ai-dial-admin-backend:latest \
  validate /data/dial-core-config.json
```

**Validate multiple files merged together:**
```bash
docker run --rm -v $(pwd)/configs:/data \
  epam/ai-dial-admin-backend:latest \
  validate --strategy MERGE_JSON /data/base.json /data/env-overrides.json
```

**Validate files sequentially (cross-file references):**
```bash
docker run --rm -v $(pwd)/configs:/data \
  epam/ai-dial-admin-backend:latest \
  validate --strategy SEQUENTIAL /data/roles.json /data/models.json
```

## CI/CD Integration

### GitHub Actions

```yaml
- name: Validate DIAL config files
  run: |
    docker run --rm \
      -v ${{ github.workspace }}/configs:/data \
      epam/ai-dial-admin-backend:${{ env.DIAL_ADMIN_TAG }} \
      validate /data/core.json /data/extensions.json
```

### GitLab CI

```yaml
validate-dial-config:
  image: docker:latest
  script:
    - docker run --rm
        -v "$CI_PROJECT_DIR/configs:/data"
        epam/ai-dial-admin-backend:${DIAL_ADMIN_TAG}
        validate /data/core.json /data/extensions.json
  rules:
    - changes:
        - configs/**/*.json
```

## Auto-Import Multi-File Configuration

The same multi-file strategies are available for the auto-import-on-bootstrap feature used by review environments.

**Environment variables:**

| Variable | Default | Description |
|----------|---------|-------------|
| `ENABLE_CONFIG_AUTO_IMPORT_ON_BOOTSTRAP` | `false` | Enable auto-import on startup |
| `IMPORT_AUTO_BOOTSTRAP_STRATEGY` | `MERGE_JSON` | `MERGE_JSON` or `SEQUENTIAL` |
| `IMPORT_AUTO_BOOTSTRAP_FILE_PATHS` | *(empty)* | Comma-separated list of file paths to import |
| `IMPORT_AUTO_BOOTSTRAP_CONFLICT_RESOLUTION` | `OVERRIDE` | `OVERRIDE` or `SKIP` |

**Example docker-compose:**
```yaml
environment:
  ENABLE_CONFIG_AUTO_IMPORT_ON_BOOTSTRAP: "true"
  IMPORT_AUTO_BOOTSTRAP_STRATEGY: "MERGE_JSON"
  IMPORT_AUTO_BOOTSTRAP_FILE_PATHS: "/configs/base.json,/configs/env-overrides.json"
volumes:
  - ./configs:/configs
```
```

**Step 2: Commit**

```bash
git add docs/cli.md
git commit -m "docs: add CLI usage guide with examples, CI snippets, and auto-import config reference"
```

---

## Task 13: Final verification

**Step 1: Run the complete test suite**

```bash
./gradlew test 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL, no failures

**Step 2: Verify checkstyle**

```bash
./gradlew checkstyleMain checkstyleTest 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

**Step 3: Verify the build produces a runnable JAR**

```bash
./gradlew bootJar 2>&1 | tail -10
```

Expected: BUILD SUCCESSFUL, JAR produced in `build/libs/`

**Step 4: Smoke-test CLI mode with a local config file (optional, requires Java)**

```bash
echo '{"models":{"test":{"displayName":"Test","endpoint":"https://test"}}}' > /tmp/test-config.json
java -jar build/libs/ai-dial-admin-backend-*.jar validate /tmp/test-config.json
```

Expected: exit 0, JSON with `"status": "valid"` printed

**Step 5: Final commit**

```bash
git add .
git commit -m "chore: final verification pass — all tests green, checkstyle clean"
```
```

---

Plan complete and saved to `docs/plans/2026-03-05-dial-admin-cli.md`.

**Two execution options:**

**1. Subagent-Driven (this session)** — I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** — Open a new session with `executing-plans`, batch execution with checkpoints

Which approach?
