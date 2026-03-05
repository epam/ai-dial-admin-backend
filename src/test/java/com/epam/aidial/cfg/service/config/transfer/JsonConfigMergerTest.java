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
