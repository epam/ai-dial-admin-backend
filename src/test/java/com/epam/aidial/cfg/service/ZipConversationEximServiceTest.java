package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ConversationEximDto;
import com.epam.aidial.cfg.dto.ConversationsEximDto;
import com.epam.aidial.cfg.mapper.ConversationMapperImpl;
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
        ConversationMapperImpl.class,
        ZipConversationEximService.class,
        ResourceImportValidator.class
})
class ZipConversationEximServiceTest {

    private static final String INVALID_EXPORT_ZIP =
            "Invalid archive format. Please upload a valid aidial-admin archive.";

    @MockitoBean
    private ConversationEximService conversationEximService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZipConversationEximService zipConversationEximService;

    @Test
    @SneakyThrows
    void importZipConversations() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var conversation = getConversationEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("conversations/conversation.json", ConversationsEximDto.builder()
                        .conversations(List.of(conversation))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var expectedImportResult = ImportResourcesResult
                .createSuccess("public/folder1/conversation1__0.0.1", "public/to/folder1/conversation1__0.0.1");
        var expectedFileResult = ImportResourcesFileResult.builder()
                .importResults(List.of(expectedImportResult))
                .build();
        when(conversationEximService.importConversations(eq(importResources), any(ConversationsEximDto.class)))
                .thenReturn(expectedFileResult);

        var importResults = zipConversationEximService.importConversations(importResources, mockMultipartFile);

        assertThat(importResults.getImportResults()).hasSize(1);
        var actualImportResult = importResults.getImportResults().get(0);
        assertThat(actualImportResult.getSourcePath()).isEqualTo("public/folder1/conversation1__0.0.1");
        assertThat(actualImportResult.getTargetPath()).isEqualTo("public/to/folder1/conversation1__0.0.1");
        assertThat(actualImportResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(actualImportResult.getError()).isNull();
    }

    @Test
    @SneakyThrows
    void importZipConversations_ZipContainConversationsFileWithoutConversationsInside_ThrowError() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("conversations/conversation.json", ConversationsEximDto.builder()
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipConversationEximService.importConversations(importResources, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo(
                "Conversation files (e.g., `conversations/*.json`) were found in the archive, "
                        + "but they do not contain conversations. Please verify the content of these files.");

        verifyNoInteractions(conversationEximService);
    }

    @Test
    @SneakyThrows
    void importZipConversations_ZipContainConflictingConversationsWithinFile_ThrowError() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var c1 = getConversationEximDto("1");
        var c2 = getConversationEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("conversations/conversation.json", ConversationsEximDto.builder()
                        .conversations(List.of(c1, c2))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipConversationEximService.importConversations(importResources, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Conversation uniqueness violation. Conflicts found:
                  Conversations duplicated within the same file:
                    - File 'conversations/conversation.json' has duplicate conversation: name 'conversation1', version '0.0.1', folder 'public/folder1/'""");

        verifyNoInteractions(conversationEximService);
    }

    @Test
    @SneakyThrows
    void importZipConversations_ZipContainConflictingConversationsAcrossFiles_ThrowError() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var c1 = getConversationEximDto("1");
        var c2 = getConversationEximDto("1");
        c2.setPrompt("changed");

        var inputStream = getZipInputStream(List.of(
                Pair.of("conversations/conversation1.json", ConversationsEximDto.builder()
                        .conversations(List.of(c1))
                        .build()),
                Pair.of("conversations/conversation2.json", ConversationsEximDto.builder()
                        .conversations(List.of(c2))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipConversationEximService.importConversations(importResources, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Conversation uniqueness violation. Conflicts found:
                  Conversations shared across different files:
                    - Conversation with name 'conversation1', version '0.0.1', folder 'public/folder1/ found in multiple files: [conversations/conversation2.json, conversations/conversation1.json]""");

        verifyNoInteractions(conversationEximService);
    }

    @Test
    @SneakyThrows
    void importZipConversations_ZipContainConflictingConversationsWithinAndAcrossFiles_ThrowError() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var c1 = getConversationEximDto("1");
        var c2 = getConversationEximDto("1");
        var c3 = getConversationEximDto("1");
        c2.setPrompt("changed description 2");
        c3.setPrompt("changed description 3");

        var inputStream = getZipInputStream(List.of(
                Pair.of("conversations/conversation1.json", ConversationsEximDto.builder()
                        .conversations(List.of(c1))
                        .build()),
                Pair.of("conversations/conversation2.json", ConversationsEximDto.builder()
                        .conversations(List.of(c2, c3))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipConversationEximService.importConversations(importResources, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Conversation uniqueness violation. Conflicts found:
                  Conversations duplicated within the same file:
                    - File 'conversations/conversation2.json' has duplicate conversation: name 'conversation1', version '0.0.1', folder 'public/folder1/'
                  Conversations shared across different files:
                    - Conversation with name 'conversation1', version '0.0.1', folder 'public/folder1/ found in multiple files: [conversations/conversation2.json, conversations/conversation1.json]""");

        verifyNoInteractions(conversationEximService);
    }

    @Test
    @SneakyThrows
    void importZipConversations_ZipDoesNotContainConversationsFileInConversationsFolder_ThrowError() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("illegal/conversation.json", ConversationsEximDto.builder()
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipConversationEximService.importConversations(importResources, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo(INVALID_EXPORT_ZIP);

        verifyNoInteractions(conversationEximService);
    }

    @Test
    @SneakyThrows
    void importZipConversations_InvalidJsonInConversationsFile_ReturnsInvalidArchiveError() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipWithRawEntry("conversations/fail.json", "{{not-json".getBytes());
        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipConversationEximService.importConversations(importResources, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo(INVALID_EXPORT_ZIP);
        verifyNoInteractions(conversationEximService);
    }

    @Test
    @SneakyThrows
    void importZipConversations_ZipContainsOnlyPathTraversalConversationPaths_ReturnsInvalidArchiveError() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var conversation = getConversationEximDto("1");
        var inputStream = getZipInputStream(List.of(
                Pair.of("test/conversations/test.json", ConversationsEximDto.builder()
                        .conversations(List.of(conversation))
                        .build())
        ));
        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipConversationEximService.importConversations(importResources, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo(INVALID_EXPORT_ZIP);
        verifyNoInteractions(conversationEximService);
    }

    @Test
    @SneakyThrows
    void previewImportConversationsFromZip() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var conversation = getConversationEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("conversations/conversation.json", ConversationsEximDto.builder()
                        .conversations(List.of(conversation))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var expectedResult = ImportResourcesPreview.builder()
                .resourcePreviews(List.of(
                        ImportResourcePreview.builder()
                                .name("conversation1")
                                .version("0.0.1")
                                .fileName("conversations/conversation.json")
                                .build()
                ))
                .build();

        var actualResult = zipConversationEximService.previewImportConversationsFromZip(importResources, mockMultipartFile);

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @SneakyThrows
    private InputStream getZipInputStream(List<Pair<String, ConversationsEximDto>> entries) {
        byte[] data;
        try (
                var baos = new ByteArrayOutputStream();
                var zos = new ZipOutputStream(baos)
        ) {
            for (var entry : entries) {
                zos.putNextEntry(new ZipEntry(entry.getLeft()));
                zos.write(objectMapper.writeValueAsBytes(entry.getRight()));
                zos.closeEntry();
            }
            zos.finish();

            data = baos.toByteArray();
        }
        return new ByteArrayInputStream(data);
    }

    @SneakyThrows
    private InputStream getZipWithRawEntry(String entryPath, byte[] rawContent) {
        byte[] data;
        try (
                var baos = new ByteArrayOutputStream();
                var zos = new ZipOutputStream(baos)
        ) {
            zos.putNextEntry(new ZipEntry(entryPath));
            zos.write(rawContent);
            zos.closeEntry();
            zos.finish();
            data = baos.toByteArray();
        }
        return new ByteArrayInputStream(data);
    }

    private ConversationEximDto getConversationEximDto(String suffix) {
        return ConversationEximDto.builder()
                .name("conversation" + suffix)
                .version(String.format("0.0.%s", suffix))
                .folderId(String.format("public/folder%s/", suffix))
                .prompt("hello")
                .build();
    }
}