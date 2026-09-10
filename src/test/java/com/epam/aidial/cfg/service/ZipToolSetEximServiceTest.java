package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.RouteMapperImpl;
import com.epam.aidial.cfg.client.mapper.ToolSetClientMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ToolSetEximDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResourcePreview;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesPreview;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.ImportResourcesStatus;
import com.epam.aidial.cfg.model.ToolSetResource;
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
        ToolSetClientMapperImpl.class,
        ZipToolSetEximService.class,
        ResourceImportValidator.class,
        RouteMapperImpl.class
})
class ZipToolSetEximServiceTest {

    private static final String INVALID_EXPORT_ZIP =
            "Invalid archive format. Please upload a valid aidial-admin archive.";

    @MockitoBean
    private ToolSetEximService toolSetEximService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZipToolSetEximService zipToolSetEximService;

    @Autowired
    private ResourceImportValidator resourceImportValidator;

    @Test
    @SneakyThrows
    void importZipToolSets() {
        // given
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var toolSet = getToolSetEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("toolSets/toolSet.json", ToolSetsEximDto.builder()
                        .toolSets(List.of(toolSet))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // Mock the behavior of toolSetEximService
        var expectedImportResult = ImportResourcesResult
                .createSuccess("public/folder1/toolSet1__0.0.1", "public/to/folder1/toolSet1__0.0.1");
        var expectedFileResult = ImportResourcesFileResult.builder()
                .importResults(List.of(expectedImportResult))
                .build();
        when(toolSetEximService.importToolSets(eq(importToolSets), any(ToolSetsEximDto.class))).thenReturn(expectedFileResult);

        // when
        var importResults = zipToolSetEximService.importToolSets(importToolSets, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var actualImportResult = importResults.getImportResults().get(0);
        assertThat(actualImportResult.getSourcePath()).isEqualTo("public/folder1/toolSet1__0.0.1");
        assertThat(actualImportResult.getTargetPath()).isEqualTo("public/to/folder1/toolSet1__0.0.1");
        assertThat(actualImportResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(actualImportResult.getError()).isNull();
    }

    @Test
    @SneakyThrows
    void importZipToolSets_ZipContainToolSetsFileWithoutToolSetsInside_ThrowError() {
        // given
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("toolSets/toolSet.json", ToolSetsEximDto.builder()
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipToolSetEximService.importToolSets(importToolSets, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("ToolSet files (e.g., `toolsets/*.json`) were found in the archive, "
                + "but they do not contain toolsets. Please verify the content of these files.");

        // Verify that toolSetEximService was not called
        verifyNoInteractions(toolSetEximService);
    }

    @Test
    @SneakyThrows
    void importZipToolSets_ZipContainConflictingToolSetsWithinFile_ThrowError() {
        // given
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var toolSet1 = getToolSetEximDto("1");
        var toolSet2 = getToolSetEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("toolSets/toolSet.json", ToolSetsEximDto.builder()
                        .toolSets(List.of(toolSet1, toolSet2))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipToolSetEximService.importToolSets(importToolSets, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                ToolSet uniqueness violation. Conflicts found:
                  ToolSets duplicated within the same file:
                    - File 'toolSets/toolSet.json' has duplicate toolset: name 'toolSet1', version '0.0.1', folder 'public/folder1/'""");

        // Verify that toolSetEximService was not called
        verifyNoInteractions(toolSetEximService);
    }

    @Test
    @SneakyThrows
    void importZipToolSets_ZipContainConflictingToolSetsAcrossFiles_ThrowError() {
        // given
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var toolSet1 = getToolSetEximDto("1");
        var toolSet2 = getToolSetEximDto("1");
        toolSet2.setDescription("changed description");

        var inputStream = getZipInputStream(List.of(
                Pair.of("toolSets/toolSet1.json", ToolSetsEximDto.builder()
                        .toolSets(List.of(toolSet1))
                        .build()),
                Pair.of("toolSets/toolSet2.json", ToolSetsEximDto.builder()
                        .toolSets(List.of(toolSet2))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipToolSetEximService.importToolSets(importToolSets, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                ToolSet uniqueness violation. Conflicts found:
                  ToolSets shared across different files:
                    - ToolSet with name 'toolSet1', version '0.0.1', folder 'public/folder1/ found in multiple files: [toolSets/toolSet2.json, toolSets/toolSet1.json]""");

        // Verify that toolSetEximService was not called
        verifyNoInteractions(toolSetEximService);
    }

    @Test
    @SneakyThrows
    void importZipToolSets_ZipContainConflictingToolSetsWithinAndAcrossFiles_ThrowError() {
        // given
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var toolSet1 = getToolSetEximDto("1");
        var toolSet2 = getToolSetEximDto("1");
        var toolSet3 = getToolSetEximDto("1");
        toolSet2.setDescription("changed description 2");
        toolSet3.setDescription("changed description 3");

        var inputStream = getZipInputStream(List.of(
                Pair.of("toolSets/toolSet1.json", ToolSetsEximDto.builder()
                        .toolSets(List.of(toolSet1))
                        .build()),
                Pair.of("toolSets/toolSet2.json", ToolSetsEximDto.builder()
                        .toolSets(List.of(toolSet2, toolSet3))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipToolSetEximService.importToolSets(importToolSets, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                ToolSet uniqueness violation. Conflicts found:
                  ToolSets duplicated within the same file:
                    - File 'toolSets/toolSet2.json' has duplicate toolset: name 'toolSet1', version '0.0.1', folder 'public/folder1/'
                  ToolSets shared across different files:
                    - ToolSet with name 'toolSet1', version '0.0.1', folder 'public/folder1/ found in multiple files: [toolSets/toolSet2.json, toolSets/toolSet1.json]""");

        // Verify that toolSetEximService was not called
        verifyNoInteractions(toolSetEximService);
    }

    @Test
    @SneakyThrows
    void importZipToolSets_ZipDoesNotContainToolSetsFileInToolSetsFolder_ThrowError() {
        // given
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("illegal/toolSet.json", ToolSetsEximDto.builder()
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipToolSetEximService.importToolSets(importToolSets, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo(INVALID_EXPORT_ZIP);

        verifyNoInteractions(toolSetEximService);
    }

    @Test
    @SneakyThrows
    void importZipToolSets_InvalidJsonInToolSetsFile_ReturnsInvalidArchiveError() {
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipWithRawEntry("toolSets/fail.json", "{{not-json".getBytes());
        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipToolSetEximService.importToolSets(importToolSets, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo(INVALID_EXPORT_ZIP);
        verifyNoInteractions(toolSetEximService);
    }

    @Test
    @SneakyThrows
    void importZipToolSets_ZipContainsOnlyPathTraversalToolSetPaths_ReturnsInvalidArchiveError() {
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var toolSet = getToolSetEximDto("1");
        var inputStream = getZipInputStream(List.of(
                Pair.of("test/toolSets/test.json", ToolSetsEximDto.builder()
                        .toolSets(List.of(toolSet))
                        .build())
        ));
        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var importResults = zipToolSetEximService.importToolSets(importToolSets, mockMultipartFile);

        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo(INVALID_EXPORT_ZIP);
        verifyNoInteractions(toolSetEximService);
    }

    @Test
    @SneakyThrows
    void previewImportToolSetsFromZip() {
        // given
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var toolSet = getToolSetEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("toolSets/toolSet.json", ToolSetsEximDto.builder()
                        .toolSets(List.of(toolSet))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var expectedResult = ImportResourcesPreview.builder()
                .resourcePreviews(List.of(
                        ImportResourcePreview.builder()
                                .name("toolSet1")
                                .version("0.0.1")
                                .fileName("toolSets/toolSet.json")
                                .build()
                ))
                .build();
        // when
        var actualResult = zipToolSetEximService.previewImportToolSetsFromZip(importToolSets, mockMultipartFile);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @SneakyThrows
    private InputStream getZipInputStream(List<Pair<String, ToolSetsEximDto>> toolSets) {
        byte[] data;
        try (
                var baos = new ByteArrayOutputStream();
                var zos = new ZipOutputStream(baos)
        ) {
            for (var entry : toolSets) {
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

    private ToolSetResource getToolSetResource(String suffix) {
        var toolSet = new ToolSetResource();
        toolSet.setName("toolSet" + suffix);
        toolSet.setDisplayName("toolSet" + suffix);
        toolSet.setVersion(String.format("0.0.%s", suffix));
        toolSet.setFolderId(String.format("public/folder%s/", suffix));
        toolSet.setPath(String.format("%s/%s__%s", toolSet.getFolderId(), toolSet.getName(), toolSet.getVersion()));
        toolSet.setDescription(String.format("toolSet description %s", suffix));
        return toolSet;
    }

    private ToolSetEximDto getToolSetEximDto(String suffix) {
        return ToolSetEximDto.builder()
                .name("toolSet" + suffix)
                .version(String.format("0.0.%s", suffix))
                .displayName("toolSet" + suffix)
                .folderId(String.format("public/folder%s/", suffix))
                .description(String.format("toolSet description %s", suffix))
                .build();
    }

}