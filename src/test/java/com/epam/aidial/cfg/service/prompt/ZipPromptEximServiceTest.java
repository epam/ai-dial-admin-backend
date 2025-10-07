package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.client.PromptClient;
import com.epam.aidial.cfg.client.mapper.PromptClientMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.PromptEximDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResourcePreview;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesPreview;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.ImportResourcesStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        JsonMapperConfiguration.class,
        PromptClientMapperImpl.class,
        ZipPromptEximService.class,
})
class ZipPromptEximServiceTest {

    @MockitoBean
    private PromptClient promptClient;
    @MockitoBean
    private PromptService promptService;
    @MockitoBean
    private PromptEximService promptEximService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZipPromptEximService zipPromptEximService;

    @Test
    @SneakyThrows
    void importZipPrompts() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var prompt = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description")
                .content("Test content")
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("prompts/prompt.json", PromptsEximDto.builder()
                        .prompts(List.of(prompt))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // Mock the behavior of promptEximService
        var expectedImportResult = ImportResourcesResult
                .createSuccess("public/PROMPT 1__1.0.0", "public/test/PROMPT 1__1.0.0");
        var expectedFileResult = ImportResourcesFileResult.builder()
                .importResults(List.of(expectedImportResult))
                .build();
        when(promptEximService.importPrompts(eq(importPrompts), any(PromptsEximDto.class))).thenReturn(expectedFileResult);

        // when
        var importResults = zipPromptEximService.importPrompts(importPrompts, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var actualImportResult = importResults.getImportResults().get(0);
        assertThat(actualImportResult.getSourcePath()).isEqualTo("public/PROMPT 1__1.0.0");
        assertThat(actualImportResult.getTargetPath()).isEqualTo("public/test/PROMPT 1__1.0.0");
        assertThat(actualImportResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(actualImportResult.getError()).isNull();
    }

    @Test
    @SneakyThrows
    void importZipPrompts_ZipContainPromptsFileWithoutPromptsInside_ThrowError() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("prompts/prompt.json", PromptsEximDto.builder()
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipPromptEximService.importPrompts(importPrompts, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("Prompt files (e.g., `prompts/*.json`) were found in the archive, "
                + "but they do not contain prompts. Please verify the content of these files.");

        // Verify that promptEximService was not called
        verifyNoInteractions(promptEximService);
        verifyNoInteractions(promptService);
    }

    @Test
    @SneakyThrows
    void importZipPrompts_ZipContainConflictingPromptsWithinFile_ThrowError() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var prompt1 = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description 1")
                .content("Test content 1")
                .build();

        var prompt2 = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description 2")
                .content("Test content 2")
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("prompts/prompt.json", PromptsEximDto.builder()
                        .prompts(List.of(prompt1, prompt2))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipPromptEximService.importPrompts(importPrompts, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Prompt ID uniqueness violation. Conflicts found:
                  Prompts duplicated within the same file:
                    - File 'prompts/prompt.json' has duplicate prompt IDs: [prompts/public/PROMPT 1__1.0.0]""");

        // Verify that promptEximService was not called
        verifyNoInteractions(promptEximService);
        verifyNoInteractions(promptService);
    }

    @Test
    @SneakyThrows
    void importZipPrompts_ZipContainConflictingPromptsAcrossFiles_ThrowError() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var prompt1 = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description 1")
                .content("Test content 1")
                .build();

        var prompt2 = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description 2")
                .content("Test content 2")
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("prompts/prompt1.json", PromptsEximDto.builder()
                        .prompts(List.of(prompt1))
                        .build()),
                Pair.of("prompts/prompt2.json", PromptsEximDto.builder()
                        .prompts(List.of(prompt2))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipPromptEximService.importPrompts(importPrompts, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Prompt ID uniqueness violation. Conflicts found:
                  Prompts shared across different files:
                    - Prompt ID 'prompts/public/PROMPT 1__1.0.0' is found in multiple files: [prompts/prompt1.json, prompts/prompt2.json]""");

        // Verify that promptEximService was not called
        verifyNoInteractions(promptEximService);
        verifyNoInteractions(promptService);
    }

    @Test
    @SneakyThrows
    void importZipPrompts_ZipContainConflictingPromptsWithinAndAcrossFiles_ThrowError() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var prompt1 = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description 1")
                .content("Test content 1")
                .build();

        var prompt2 = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description 2")
                .content("Test content 2")
                .build();

        var prompt3 = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description 3")
                .content("Test content 3")
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("prompts/prompt1.json", PromptsEximDto.builder()
                        .prompts(List.of(prompt1))
                        .build()),
                Pair.of("prompts/prompt2.json", PromptsEximDto.builder()
                        .prompts(List.of(prompt2, prompt3))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipPromptEximService.importPrompts(importPrompts, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Prompt ID uniqueness violation. Conflicts found:
                  Prompts duplicated within the same file:
                    - File 'prompts/prompt2.json' has duplicate prompt IDs: [prompts/public/PROMPT 1__1.0.0]
                  Prompts shared across different files:
                    - Prompt ID 'prompts/public/PROMPT 1__1.0.0' is found in multiple files: [prompts/prompt1.json, prompts/prompt2.json]""");

        // Verify that promptEximService was not called
        verifyNoInteractions(promptEximService);
        verifyNoInteractions(promptService);
    }

    @Test
    @SneakyThrows
    void importZipPrompts_ZipDoesNotContainPromptsFileInPromptsFolder_ThrowError() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("illegal/prompt.json", PromptsEximDto.builder()
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipPromptEximService.importPrompts(importPrompts, mockMultipartFile);

        // then
        // Since the zip doesn't contain prompts.json, the method should return an empty result with an error
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("No prompt files (e.g., `prompts/*.json`) found or loaded from the archive. "
                + "Please ensure prompt files are placed in a `prompts/` directory and have a `.json` extension.");

        // Verify that promptEximService was not called
        verifyNoInteractions(promptEximService);
        verifyNoInteractions(promptService);
    }

    @Test
    @SneakyThrows
    void previewImportPromptsFromZip() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var prompt = PromptEximDto.builder()
                .id("prompts/public/PROMPT 1__1.0.0")
                .description("Test description")
                .content("Test content")
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("prompts/prompt.json", PromptsEximDto.builder()
                        .prompts(List.of(prompt))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var expectedResult = ImportResourcesPreview.builder()
                .resourcePreviews(List.of(
                        ImportResourcePreview.builder()
                                .name("PROMPT 1")
                                .version("1.0.0")
                                .fileName("prompts/prompt.json")
                                .build()
                ))
                .build();
        // when
        var actualResult = zipPromptEximService.previewImportPromptsFromZip(importPrompts, mockMultipartFile);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @SneakyThrows
    private InputStream getZipInputStream(List<Pair<String, PromptsEximDto>> prompts) {
        byte[] data;
        try (
                var baos = new ByteArrayOutputStream();
                var zos = new ZipOutputStream(baos)
        ) {
            for (var entry : prompts) {
                zos.putNextEntry(new ZipEntry(entry.getLeft()));
                zos.write(objectMapper.writeValueAsBytes(entry.getRight()));
                zos.closeEntry();
            }
            zos.finish();

            data = baos.toByteArray();
        }

        return new ByteArrayInputStream(data);
    }

}
