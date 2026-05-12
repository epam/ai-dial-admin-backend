package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.RouteMapperImpl;
import com.epam.aidial.cfg.client.mapper.ToolSetClientMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ToolSetEximDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesStatus;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResourceNodeInfo;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.utils.ExportPathUtils;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        JsonMapperConfiguration.class,
        ToolSetClientMapperImpl.class,
        ToolSetEximService.class,
        ResourceImportValidator.class,
        RouteMapperImpl.class
})
@TestPropertySource(properties = {
        "toolsets.import.consecutiveErrorsThreshold=2"
})
class ToolSetEximServiceTest {

    @MockitoBean
    private ToolSetResourceService toolSetService;
    @MockitoBean
    private FolderService folderService;

    @Autowired
    private ToolSetEximService toolSetEximService;

    @Test
    @SneakyThrows
    void exportToolSets_SinglePath() {
        // given
        var toolSetResource = getToolSetResource("1");
        var path = toolSetResource.getPath();

        when(toolSetService.getToolSetResource(path)).thenReturn(toolSetResource);

        // when
        var result = toolSetEximService.exportToolSets(List.of(path));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getToolSets()).hasSize(1);

        var toolSetExim = result.getToolSets().get(0);
        assertThat(toolSetExim.getDisplayName()).isEqualTo("toolSet1");
        assertThat(toolSetExim.getFolderId()).isEqualTo("public/");
        assertThat(toolSetExim.getDescription()).isEqualTo("toolSet description 1");
        assertThat(toolSetExim.isForwardPerRequestKey()).isTrue();
    }

    @Test
    @SneakyThrows
    void exportToolSets_MultiplePaths() {
        // given
        var toolSetResource1 = getToolSetResource("1");
        var toolSetResource2 = getToolSetResource("2");

        var path1 = toolSetResource1.getPath();
        var path2 = toolSetResource2.getPath();

        when(toolSetService.getToolSetResource(path1)).thenReturn(toolSetResource1);
        when(toolSetService.getToolSetResource(path2)).thenReturn(toolSetResource2);

        // when
        var result = toolSetEximService.exportToolSets(List.of(path1, path2));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getToolSets()).hasSize(2);

        // Verify first toolSet
        var toolSetExim1 = result.getToolSets().get(0);
        assertThat(toolSetExim1.getName()).isEqualTo("toolSet1");
        assertThat(toolSetExim1.getFolderId()).isEqualTo("public/");
        assertThat(toolSetExim1.getDescription()).isEqualTo("toolSet description 1");
        assertThat(toolSetExim1.isForwardPerRequestKey()).isTrue();

        // Verify second toolSet1Exim1
        var toolSetExim2 = result.getToolSets().get(1);
        assertThat(toolSetExim2.getName()).isEqualTo("toolSet2");
        assertThat(toolSetExim2.getFolderId()).isEqualTo("public/");
        assertThat(toolSetExim2.getDescription()).isEqualTo("toolSet description 2");
        assertThat(toolSetExim2.isForwardPerRequestKey()).isTrue();
    }

    @Test
    @SneakyThrows
    void exportToolSets_FolderPath() {
        var folderPath = "public/folder1/folder2/folder3/";
        var toolSetStoragePath = "public/folder1/folder2/folder3/folder4/toolSet__0.0.1";

        var item = ToolSetResourceNodeInfo.builder()
                .nodeType(NodeType.ITEM)
                .path(toolSetStoragePath)
                .build();
        var folder = ToolSetResourceNodeInfo.builder()
                .nodeType(NodeType.FOLDER)
                .items(List.of(item))
                .build();

        when(toolSetService.getToolSetResources(argThat(req ->
                folderPath.equals(req.getPath()) && req.isRecursive()))).thenReturn(folder);

        var toolSet = new ToolSetResource();
        toolSet.setPath(toolSetStoragePath);
        toolSet.setName("toolSet");
        toolSet.setVersion("0.0.1");
        toolSet.setFolderId("public/folder1/folder2/folder3/folder4/");
        toolSet.setForwardPerRequestKey(true);

        when(toolSetService.getToolSetResource(toolSetStoragePath)).thenReturn(toolSet);

        var result = toolSetEximService.exportToolSets(List.of(folderPath));

        assertThat(result.getToolSets()).hasSize(1);
        var exim = result.getToolSets().get(0);
        assertThat(exim.getName()).isEqualTo("toolSet");
        assertThat(exim.getFolderId()).isEqualTo("public/folder3/folder4/");
        verify(toolSetService).getToolSetResource(toolSetStoragePath);
    }

    @Test
    @SneakyThrows
    void exportToolSets_FolderPath_excludesTechnicalFile() {
        var folderPath = "public/folder1/folder2/";
        var path = "public/folder1/folder2/toolSet__0.0.1";
        var techPath = "public/folder1/folder2/" + ExportPathUtils.DIAL_FOLDER_FILE + "__0.0.1";

        var techItem = ToolSetResourceNodeInfo.builder()
                .nodeType(NodeType.ITEM)
                .path(techPath)
                .build();
        var item = ToolSetResourceNodeInfo.builder()
                .nodeType(NodeType.ITEM)
                .path(path)
                .build();
        var folder = ToolSetResourceNodeInfo.builder()
                .nodeType(NodeType.FOLDER)
                .items(List.of(techItem, item))
                .build();

        when(toolSetService.getToolSetResources(argThat(req ->
                folderPath.equals(req.getPath()) && req.isRecursive()))).thenReturn(folder);

        var toolSet = new ToolSetResource();
        toolSet.setPath(path);
        toolSet.setName("toolSet");
        toolSet.setVersion("0.0.1");
        toolSet.setFolderId("public/folder1/folder2/");
        toolSet.setForwardPerRequestKey(true);

        when(toolSetService.getToolSetResource(path)).thenReturn(toolSet);

        var result = toolSetEximService.exportToolSets(List.of(folderPath));

        assertThat(result.getToolSets()).hasSize(1);
        assertThat(result.getToolSets().get(0).getName()).isEqualTo("toolSet");
        assertThat(result.getToolSets().get(0).getFolderId()).isEqualTo("public/folder2/");
        verify(toolSetService).getToolSetResource(path);
    }

    @Test
    @SneakyThrows
    void exportToolSets_EmptyPaths_ReturnsEmptyResult() {
        // when
        var result = toolSetEximService.exportToolSets(Collections.emptyList());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getToolSets()).isEmpty();

        verifyNoInteractions(toolSetService);
    }

    @Test
    void exportToolSets_ToolSetClientThrowsException_PropagatesException() {
        // given
        var path = "public/folder1/toolset1__0.0.1";
        var exception = new RuntimeException("Test exception");

        when(toolSetService.getToolSetResource(anyString())).thenThrow(exception);

        // when/then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> toolSetEximService.exportToolSets(List.of(path)),
                "Expected export ToolSets to throw RuntimeException"
        );

        // Verify the exception cause
        assertThat(thrown.getCause()).isEqualTo(exception);
    }

    @Test
    @SneakyThrows
    void importToolSets_NotFlatImport() {
        // given
        var path = "public/to/";
        var rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        var rules = List.of(rule);
        var importToolSets = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .rules(rules)
                .build();

        var toolSetExim = getToolSetEximDto("1");
        var toolSetsExim = new ToolSetsEximDto();
        toolSetsExim.setToolSets(List.of(toolSetExim));

        var updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(path)
                .rules(rules)
                .build();
        doNothing().when(folderService).updatesRules(updateRulesRequest);

        // when
        var importResults = toolSetEximService.importToolSets(importToolSets, toolSetsExim);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var importResult = importResults.getImportResults().get(0);
        assertThat(importResult.getSourcePath()).isEqualTo("public/folder1/toolSet1__0.0.1");
        assertThat(importResult.getTargetPath()).isEqualTo("public/to/folder1/toolSet1__0.0.1");
        assertThat(importResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(importResult.getError()).isNull();

        var captor = ArgumentCaptor.forClass(CreateToolSetResource.class);
        verify(toolSetService).putToolSetResource(captor.capture(), eq(true), isNull());
        verify(folderService).updatesRules(updateRulesRequest);

        var toolSetEximResource = captor.getValue();
        assertThat(toolSetEximResource.getName()).isEqualTo("toolSet1");
        assertThat(toolSetEximResource.getVersion()).isEqualTo("0.0.1");
        assertThat(toolSetEximResource.getFolderId()).isEqualTo("public/to/folder1/");
        assertThat(toolSetEximResource.getDescription()).isEqualTo("toolSet description 1");
    }

    @Test
    @SneakyThrows
    void importToolSets_FlatImport() {
        // given
        var path = "public/to/";
        var rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        var rules = List.of(rule);
        var importToolSets = ImportResources.builder()
                .path(path)
                .flatImport(true)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .rules(rules)
                .build();

        var toolSetExim = getToolSetEximDto("1");
        var toolSetsExim = new ToolSetsEximDto();
        toolSetsExim.setToolSets(List.of(toolSetExim));

        var updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(path)
                .rules(rules)
                .build();
        doNothing().when(folderService).updatesRules(updateRulesRequest);

        // when
        var importResults = toolSetEximService.importToolSets(importToolSets, toolSetsExim);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var importResult = importResults.getImportResults().get(0);
        assertThat(importResult.getSourcePath()).isEqualTo("public/folder1/toolSet1__0.0.1");
        assertThat(importResult.getTargetPath()).isEqualTo("public/to/toolSet1__0.0.1");
        assertThat(importResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(importResult.getError()).isNull();

        var captor = ArgumentCaptor.forClass(CreateToolSetResource.class);
        verify(toolSetService).putToolSetResource(captor.capture(), eq(true), isNull());
        verify(folderService).updatesRules(updateRulesRequest);

        var toolSet = captor.getValue();
        assertThat(toolSet.getName()).isEqualTo("toolSet1");
        assertThat(toolSet.getVersion()).isEqualTo("0.0.1");
        assertThat(toolSet.getFolderId()).isEqualTo("public/to/");
        assertThat(toolSet.getDescription()).isEqualTo("toolSet description 1");
        assertThat(toolSet.isForwardPerRequestKey()).isFalse();
    }

    @Test
    @SneakyThrows
    void importToolSets_DuplicatedResources_PerItemFailuresAndNoImport() {
        var importToolSets = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var toolSetsExim = new ToolSetsEximDto();
        toolSetsExim.setToolSets(List.of(getToolSetEximDto("1"), getToolSetEximDto("1")));

        var importResults = toolSetEximService.importToolSets(importToolSets, toolSetsExim);

        assertThat(importResults.getImportResults()).hasSize(2);
        assertThat(importResults.getImportResults()).allMatch(r -> r.getStatus() == ImportResourcesStatus.FAILURE);
        assertThat(importResults.getImportResults().get(0).getError()).contains("Duplicated toolset");
        verifyNoInteractions(toolSetService);
    }

    @Test
    @SneakyThrows
    void importToolSets_UniquenessConflict_ImportsNonDuplicateItems() {
        var path = "public/folder/";
        var importToolSets = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var toolSetsExim = new ToolSetsEximDto();
        toolSetsExim.setToolSets(List.of(getToolSetEximDto("1"), getToolSetEximDto("2"), getToolSetEximDto("2")));

        var captor = ArgumentCaptor.forClass(CreateToolSetResource.class);

        var importResults = toolSetEximService.importToolSets(importToolSets, toolSetsExim);

        assertThat(importResults.getImportResults()).hasSize(3);
        assertThat(importResults.getImportResults().get(0).getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(importResults.getImportResults().get(1).getStatus()).isEqualTo(ImportResourcesStatus.FAILURE);
        assertThat(importResults.getImportResults().get(2).getStatus()).isEqualTo(ImportResourcesStatus.FAILURE);

        verify(toolSetService).putToolSetResource(captor.capture(), eq(true), isNull());
    }

    private ToolSetResource getToolSetResource(String suffix) {
        var toolSet = new ToolSetResource();
        toolSet.setName("toolSet" + suffix);
        toolSet.setDisplayName("toolSet" + suffix);
        toolSet.setVersion(String.format("0.0.%s", suffix));
        toolSet.setFolderId(String.format("public/folder%s/", suffix));
        toolSet.setPath(String.format("%s%s__%s", toolSet.getFolderId(), toolSet.getName(), toolSet.getVersion()));
        toolSet.setDescription(String.format("toolSet description %s", suffix));
        toolSet.setForwardPerRequestKey(true);
        return toolSet;
    }

    private ToolSetEximDto getToolSetEximDto(String suffix) {
        return ToolSetEximDto.builder()
                .name("toolSet" + suffix)
                .version(String.format("0.0.%s", suffix))
                .displayName("toolSet" + suffix)
                .folderId(String.format("public/folder%s/", suffix))
                .description(String.format("toolSet description %s", suffix))
                .forwardPerRequestKey(false)
                .build();
    }
}