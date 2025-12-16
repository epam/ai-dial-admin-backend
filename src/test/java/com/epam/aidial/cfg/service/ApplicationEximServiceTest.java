package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.ApplicationClientMapperImpl;
import com.epam.aidial.cfg.client.mapper.RouteMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesStatus;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        JsonMapperConfiguration.class,
        ApplicationClientMapperImpl.class,
        ApplicationEximService.class,
        RouteMapperImpl.class
})
@TestPropertySource(properties = {
        "applications.import.consecutiveErrorsThreshold=2"
})
class ApplicationEximServiceTest {

    @MockitoBean
    private ApplicationResourceService applicationService;
    @MockitoBean
    private FolderService folderService;
    @MockitoBean
    private ResourceImportValidator validator;

    @Autowired
    private ApplicationEximService applicationEximService;

    @Test
    @SneakyThrows
    void exportApplications_SinglePath() {
        // given
        var application = getApplicationResource("1");
        var path = application.getPath();

        when(applicationService.getApplicationResource(path)).thenReturn(application);

        // when
        var result = applicationEximService.exportApplications(List.of(path));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getApplications()).hasSize(1);

        var applicationExim = result.getApplications().get(0);
        assertThat(applicationExim.getApplicationTypeSchemaId()).isEqualTo("https://test1.epam.com");
        assertThat(applicationExim.getDisplayName()).isEqualTo("application1");
        assertThat(applicationExim.getFolderId()).isEqualTo("public/folder1/");
        assertThat(applicationExim.getDescription()).isEqualTo("application description 1");
    }

    @Test
    @SneakyThrows
    void exportApplications_MultiplePaths() {
        // given
        var application1 = getApplicationResource("1");
        var application2 = getApplicationResource("2");

        var path1 = application1.getPath();
        var path2 = application2.getPath();

        when(applicationService.getApplicationResource(path1)).thenReturn(application1);
        when(applicationService.getApplicationResource(path2)).thenReturn(application2);

        // when
        var result = applicationEximService.exportApplications(List.of(path1, path2));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getApplications()).hasSize(2);

        // Verify first application
        var application1Exim1 = result.getApplications().get(0);
        assertThat(application1Exim1.getApplicationTypeSchemaId()).isEqualTo("https://test1.epam.com");
        assertThat(application1Exim1.getName()).isEqualTo("application1");
        assertThat(application1Exim1.getFolderId()).isEqualTo("public/folder1/");
        assertThat(application1Exim1.getDescription()).isEqualTo("application description 1");

        // Verify second application
        var application1Exim2 = result.getApplications().get(1);
        assertThat(application1Exim2.getApplicationTypeSchemaId()).isEqualTo("https://test2.epam.com");
        assertThat(application1Exim2.getName()).isEqualTo("application2");
        assertThat(application1Exim2.getFolderId()).isEqualTo("public/folder2/");
        assertThat(application1Exim2.getDescription()).isEqualTo("application description 2");
    }

    @Test
    @SneakyThrows
    void exportApplications_EmptyPaths_ReturnsEmptyResult() {
        // when
        var result = applicationEximService.exportApplications(Collections.emptyList());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getApplications()).isEmpty();

        verifyNoInteractions(applicationService);
    }

    @Test
    void exportApplications_ApplicationClientThrowsException_PropagatesException() {
        // given
        var path = "public/folder1/application1__0.0.1";
        var exception = new RuntimeException("Test exception");

        when(applicationService.getApplicationResource(anyString())).thenThrow(exception);

        // when/then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> applicationEximService.exportApplications(List.of(path)),
                "Expected exportApplications to throw RuntimeException"
        );

        // Verify the exception cause
        assertThat(thrown.getCause()).isEqualTo(exception);
    }

    @Test
    @SneakyThrows
    void importApplications_NotFlatImport() {
        // given
        var path = "public/to/";
        var rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        var rules = List.of(rule);
        var importApplications = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .rules(rules)
                .build();

        var applicationExim = getApplicationEximDto("1");
        var applicationsExim = new ApplicationsEximDto();
        applicationsExim.setApplications(List.of(applicationExim));

        var updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(path)
                .rules(rules)
                .build();
        doNothing().when(folderService).updatesRules(updateRulesRequest);

        // when
        var importResults = applicationEximService.importApplications(importApplications, applicationsExim);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var importResult = importResults.getImportResults().get(0);
        assertThat(importResult.getSourcePath()).isEqualTo("public/folder1/application1__0.0.1");
        assertThat(importResult.getTargetPath()).isEqualTo("public/to/folder1/application1__0.0.1");
        assertThat(importResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(importResult.getError()).isNull();

        var captor = ArgumentCaptor.forClass(CreateApplicationResource.class);
        verify(applicationService).putApplicationResource(captor.capture(), eq(true), isNull());
        verify(folderService).updatesRules(updateRulesRequest);

        var applicationResource = captor.getValue();
        assertThat(applicationResource.getName()).isEqualTo("application1");
        assertThat(applicationResource.getVersion()).isEqualTo("0.0.1");
        assertThat(applicationResource.getFolderId()).isEqualTo("public/to/folder1/");
        assertThat(applicationResource.getDescription()).isEqualTo("application description 1");
    }

    @Test
    @SneakyThrows
    void importApplications_FlatImport() {
        // given
        var path = "public/to/";
        var rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        var rules = List.of(rule);
        var importApplications = ImportResources.builder()
                .path(path)
                .flatImport(true)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .rules(rules)
                .build();

        var applicationExim = getApplicationEximDto("1");
        var applicationsExim = new ApplicationsEximDto();
        applicationsExim.setApplications(List.of(applicationExim));

        var updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(path)
                .rules(rules)
                .build();
        doNothing().when(folderService).updatesRules(updateRulesRequest);

        // when
        var importResults = applicationEximService.importApplications(importApplications, applicationsExim);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var importResult = importResults.getImportResults().get(0);
        assertThat(importResult.getSourcePath()).isEqualTo("public/folder1/application1__0.0.1");
        assertThat(importResult.getTargetPath()).isEqualTo("public/to/application1__0.0.1");
        assertThat(importResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(importResult.getError()).isNull();

        var captor = ArgumentCaptor.forClass(CreateApplicationResource.class);
        verify(applicationService).putApplicationResource(captor.capture(), eq(true), isNull());
        verify(folderService).updatesRules(updateRulesRequest);

        var application = captor.getValue();
        assertThat(application.getName()).isEqualTo("application1");
        assertThat(application.getVersion()).isEqualTo("0.0.1");
        assertThat(application.getFolderId()).isEqualTo("public/to/");
        assertThat(application.getDescription()).isEqualTo("application description 1");
    }

    @Test
    @SneakyThrows
    void importApplications_ValidatorThrowsError_RethrowsError() {
        // given
        var importApplications = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var applicationExim = getApplicationEximDto("1");
        var applicationsExim = new ApplicationsEximDto();
        applicationsExim.setApplications(List.of(applicationExim));

        doThrow(new IllegalArgumentException("Validation error"))
                .when(validator).validateApplicationImport(importApplications, applicationsExim);

        // when/then
        var thrown = assertThrows(IllegalArgumentException.class,
                () -> applicationEximService.importApplications(importApplications, applicationsExim));

        assertThat(thrown).hasMessage("Validation error");
        verifyNoInteractions(applicationService);
    }

    private ApplicationResource getApplicationResource(String suffix) {
        var application = new ApplicationResource();
        application.setApplicationTypeSchemaId(String.format("https://test%s.epam.com", suffix));
        application.setName("application" + suffix);
        application.setDisplayName("application" + suffix);
        application.setVersion(String.format("0.0.%s", suffix));
        application.setFolderId(String.format("public/folder%s/", suffix));
        application.setPath(String.format("%s%s__%s", application.getFolderId(), application.getName(), application.getVersion()));
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