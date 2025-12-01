package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.ApplicationClientMapperImpl;
import com.epam.aidial.cfg.client.mapper.RouteMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.model.ApplicationResource;
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
        ApplicationClientMapperImpl.class,
        ZipApplicationEximService.class,
        RouteMapperImpl.class
})
class ZipApplicationEximServiceTest {

    @MockitoBean
    private ApplicationEximService applicationEximService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZipApplicationEximService zipApplicationEximService;

    @Test
    @SneakyThrows
    void importZipApplications() {
        // given
        var importApplications = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var application = getApplicationEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("applications/application.json", ApplicationsEximDto.builder()
                        .applications(List.of(application))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // Mock the behavior of applicationEximService
        var expectedImportResult = ImportResourcesResult
                .createSuccess("public/folder1/application1__0.0.1", "public/to/folder1/application1__0.0.1");
        var expectedFileResult = ImportResourcesFileResult.builder()
                .importResults(List.of(expectedImportResult))
                .build();
        when(applicationEximService.importApplications(eq(importApplications), any(ApplicationsEximDto.class))).thenReturn(expectedFileResult);

        // when
        var importResults = zipApplicationEximService.importApplications(importApplications, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var actualImportResult = importResults.getImportResults().get(0);
        assertThat(actualImportResult.getSourcePath()).isEqualTo("public/folder1/application1__0.0.1");
        assertThat(actualImportResult.getTargetPath()).isEqualTo("public/to/folder1/application1__0.0.1");
        assertThat(actualImportResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(actualImportResult.getError()).isNull();
    }

    @Test
    @SneakyThrows
    void importZipApplications_ZipContainApplicationsFileWithoutApplicationsInside_ThrowError() {
        // given
        var importApplications = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("applications/application.json", ApplicationsEximDto.builder()
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipApplicationEximService.importApplications(importApplications, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("Application files (e.g., `applications/*.json`) were found in the archive, "
                + "but they do not contain applications. Please verify the content of these files.");

        // Verify that applicationEximService was not called
        verifyNoInteractions(applicationEximService);
    }

    @Test
    @SneakyThrows
    void importZipApplications_ZipContainConflictingApplicationsWithinFile_ThrowError() {
        // given
        var importApplications = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("applications/application.json", ApplicationsEximDto.builder()
                        .applications(List.of(application1, application2))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipApplicationEximService.importApplications(importApplications, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Application ID uniqueness violation. Conflicts found:
                  Applications duplicated within the same file:
                    - File 'applications/application.json' has duplicate application IDs: [https://test1.epam.com]""");

        // Verify that applicationEximService was not called
        verifyNoInteractions(applicationEximService);
    }

    @Test
    @SneakyThrows
    void importZipApplications_ZipContainConflictingApplicationsAcrossFiles_ThrowError() {
        // given
        var importApplications = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        application2.setDescription("changed description");

        var inputStream = getZipInputStream(List.of(
                Pair.of("applications/application1.json", ApplicationsEximDto.builder()
                        .applications(List.of(application1))
                        .build()),
                Pair.of("applications/application2.json", ApplicationsEximDto.builder()
                        .applications(List.of(application2))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipApplicationEximService.importApplications(importApplications, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Application ID uniqueness violation. Conflicts found:
                  Applications shared across different files:
                    - Application ID 'https://test1.epam.com' is found in multiple files: [applications/application2.json, applications/application1.json]""");

        // Verify that applicationEximService was not called
        verifyNoInteractions(applicationEximService);
    }

    @Test
    @SneakyThrows
    void importZipApplications_ZipContainConflictingApplicationsWithinAndAcrossFiles_ThrowError() {
        // given
        var importApplications = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var application1 = getApplicationEximDto("1");
        var application2 = getApplicationEximDto("1");
        var application3 = getApplicationEximDto("1");
        application2.setDescription("changed description 2");
        application3.setDescription("changed description 3");

        var inputStream = getZipInputStream(List.of(
                Pair.of("applications/application1.json", ApplicationsEximDto.builder()
                        .applications(List.of(application1))
                        .build()),
                Pair.of("applications/application2.json", ApplicationsEximDto.builder()
                        .applications(List.of(application2, application3))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipApplicationEximService.importApplications(importApplications, mockMultipartFile);

        // then
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("""
                Application ID uniqueness violation. Conflicts found:
                  Applications duplicated within the same file:
                    - File 'applications/application2.json' has duplicate application IDs: [https://test1.epam.com]
                  Applications shared across different files:
                    - Application ID 'https://test1.epam.com' is found in multiple files: [applications/application2.json, applications/application1.json]""");

        // Verify that applicationEximService was not called
        verifyNoInteractions(applicationEximService);
    }

    @Test
    @SneakyThrows
    void importZipApplications_ZipDoesNotContainApplicationsFileInApplicationsFolder_ThrowError() {
        // given
        var importApplications = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var inputStream = getZipInputStream(List.of(
                Pair.of("illegal/application.json", ApplicationsEximDto.builder()
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        // when
        var importResults = zipApplicationEximService.importApplications(importApplications, mockMultipartFile);

        // then
        // Since the zip doesn't contain applications.json, the method should return an empty result with an error
        assertThat(importResults.getImportResults()).isEmpty();
        assertThat(importResults.getError()).isEqualTo("No application files (e.g., `applications/*.json`) found or loaded from the archive. "
                + "Please ensure application files are placed in a `applications/` directory and have a `.json` extension.");

        // Verify that applicationEximService was not called
        verifyNoInteractions(applicationEximService);
    }

    @Test
    @SneakyThrows
    void previewImportApplicationsFromZip() {
        // given
        var importApplications = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var application = getApplicationEximDto("1");

        var inputStream = getZipInputStream(List.of(
                Pair.of("applications/application.json", ApplicationsEximDto.builder()
                        .applications(List.of(application))
                        .build())
        ));

        var mockMultipartFile = new MockMultipartFile("file", inputStream);

        var expectedResult = ImportResourcesPreview.builder()
                .resourcePreviews(List.of(
                        ImportResourcePreview.builder()
                                .name("application1")
                                .version("0.0.1")
                                .fileName("applications/application.json")
                                .build()
                ))
                .build();
        // when
        var actualResult = zipApplicationEximService.previewImportApplicationsFromZip(importApplications, mockMultipartFile);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @SneakyThrows
    private InputStream getZipInputStream(List<Pair<String, ApplicationsEximDto>> applications) {
        byte[] data;
        try (
                var baos = new ByteArrayOutputStream();
                var zos = new ZipOutputStream(baos)
        ) {
            for (var entry : applications) {
                zos.putNextEntry(new ZipEntry(entry.getLeft()));
                zos.write(objectMapper.writeValueAsBytes(entry.getRight()));
                zos.closeEntry();
            }
            zos.finish();

            data = baos.toByteArray();
        }
        return new ByteArrayInputStream(data);
    }

    private ApplicationResource getApplicationResource(String suffix) {
        var application = new ApplicationResource();
        application.setApplicationTypeSchemaId(String.format("https://test%s.epam.com", suffix));
        application.setName("application" + suffix);
        application.setDisplayName("application" + suffix);
        application.setVersion(String.format("0.0.%s", suffix));
        application.setFolderId(String.format("public/folder%s/", suffix));
        application.setPath(String.format("%s/%s__%s", application.getFolderId(), application.getName(), application.getVersion()));
        application.setDescription(String.format("application description %s", suffix));
        return application;
    }

    private ApplicationEximDto getApplicationEximDto(String suffix) {
        return ApplicationEximDto.builder()
                .applicationTypeSchemaId(String.format("https://test%s.epam.com", suffix))
                .name("application" + suffix)
                .version(String.format("0.0.%s", suffix))
                .displayName("application" + suffix)
                .folderId(String.format("public/folder%s/", suffix))
                .description(String.format("application description %s", suffix))
                .build();
    }

}