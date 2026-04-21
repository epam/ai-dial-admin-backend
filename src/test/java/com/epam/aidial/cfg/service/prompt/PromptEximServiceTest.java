package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.client.mapper.PromptClientMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.PromptEximDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.model.CreatePrompt;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesStatus;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.FolderService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        JsonMapperConfiguration.class,
        PromptClientMapperImpl.class,
        PromptEximService.class,
})
@TestPropertySource(properties = {
        "prompts.import.consecutiveErrorsThreshold=2"
})
class PromptEximServiceTest {

    private static final String PROMPTS_FOLDER = "prompts/";

    @MockitoBean
    private PromptService promptService;
    @MockitoBean
    private FolderService folderService;
    @MockitoBean
    private PromptImportValidator validator;

    @Autowired
    private PromptEximService promptEximService;

    @BeforeEach
    void stubUniquenessValidator() {
        when(validator.collectUniquenessConflicts(any(), any())).thenReturn(Map.of());
    }

    @Test
    @SneakyThrows
    void exportPrompts_SinglePath() {
        // given
        var path = "public/folder/PROMPT 1__1.0.0";
        var prompt = new Prompt();
        prompt.setPath("public/folder/PROMPT 1__1.0.0");
        prompt.setName("PROMPT 1");
        prompt.setFolderId("prompts/public/folder/");
        prompt.setDescription("Test description");
        prompt.setContent("Test content");

        when(promptService.getPrompt(path)).thenReturn(prompt);

        // when
        var result = promptEximService.exportPrompts(List.of(path));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrompts()).hasSize(1);
        assertThat(result.getFolders()).hasSize(1);

        var promptExim = result.getPrompts().get(0);
        assertThat(promptExim.getId()).isEqualTo(PROMPTS_FOLDER + "public/folder/PROMPT 1__1.0.0");
        assertThat(promptExim.getName()).isEqualTo("PROMPT 1");
        assertThat(promptExim.getFolderId()).isEqualTo(PROMPTS_FOLDER + "public/folder/");
        assertThat(promptExim.getDescription()).isEqualTo("Test description");
        assertThat(promptExim.getContent()).isEqualTo("Test content");

        var folderExim = result.getFolders().get(0);
        assertThat(folderExim.getId()).isEqualTo(PROMPTS_FOLDER + "public/folder");
        assertThat(folderExim.getName()).isEqualTo("folder");
        assertThat(folderExim.getFolderId()).isEqualTo(PROMPTS_FOLDER + "public/");
        assertThat(folderExim.getType()).isEqualTo("prompt");
    }

    @Test
    @SneakyThrows
    void exportPrompts_MultiplePaths() {
        // given
        var path1 = "public/folder1/PROMPT 1__1.0.0";
        var path2 = "public/folder2/PROMPT 2__1.0.0";

        var prompt1 = new Prompt();
        prompt1.setPath("public/folder1/PROMPT 1__1.0.0");
        prompt1.setName("PROMPT 2");
        prompt1.setFolderId("prompts/public/folder1/");
        prompt1.setDescription("Test description 1");
        prompt1.setContent("Test content 1");

        var prompt2 = new Prompt();
        prompt2.setPath("public/folder2/PROMPT%202__1.0.0");
        prompt2.setName("PROMPT 2");
        prompt2.setFolderId("prompts/public/folder2/");
        prompt2.setDescription("Test description 2");
        prompt2.setContent("Test content 2");

        when(promptService.getPrompt(path1)).thenReturn(prompt1);
        when(promptService.getPrompt(path2)).thenReturn(prompt2);

        // when
        var result = promptEximService.exportPrompts(List.of(path1, path2));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrompts()).hasSize(2);
        assertThat(result.getFolders()).hasSize(2);

        // Verify first prompt
        var promptExim1 = result.getPrompts().get(0);
        assertThat(promptExim1.getId()).isEqualTo(PROMPTS_FOLDER + "public/folder1/PROMPT 1__1.0.0");
        assertThat(promptExim1.getName()).isEqualTo("PROMPT 1");
        assertThat(promptExim1.getFolderId()).isEqualTo(PROMPTS_FOLDER + "public/folder1/");
        assertThat(promptExim1.getDescription()).isEqualTo("Test description 1");
        assertThat(promptExim1.getContent()).isEqualTo("Test content 1");

        // Verify second prompt
        var promptExim2 = result.getPrompts().get(1);
        assertThat(promptExim2.getId()).isEqualTo(PROMPTS_FOLDER + "public/folder2/PROMPT 2__1.0.0");
        assertThat(promptExim2.getName()).isEqualTo("PROMPT 2");
        assertThat(promptExim2.getFolderId()).isEqualTo(PROMPTS_FOLDER + "public/folder2/");
        assertThat(promptExim2.getDescription()).isEqualTo("Test description 2");
        assertThat(promptExim2.getContent()).isEqualTo("Test content 2");
    }

    @Test
    @SneakyThrows
    void exportPrompts_EmptyPaths_ReturnsEmptyResult() {
        // when
        var result = promptEximService.exportPrompts(Collections.emptyList());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrompts()).isEmpty();
        assertThat(result.getFolders()).isEmpty();

        verifyNoInteractions(promptService);
    }

    @Test
    void exportPrompts_PromptClientThrowsException_PropagatesException() {
        // given
        var path = "public/folder/PROMPT%201__1.0.0";
        var exception = new RuntimeException("Test exception");

        when(promptService.getPrompt(anyString())).thenThrow(exception);

        // when/then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> promptEximService.exportPrompts(List.of(path)),
                "Expected exportPrompts to throw RuntimeException"
        );

        // Verify the exception cause
        assertThat(thrown.getCause()).isEqualTo(exception);
    }

    @Test
    @SneakyThrows
    void importPrompts() {
        // given
        var path = "public/to/";
        var rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        var rules = List.of(rule);
        var importPrompts = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .rules(rules)
                .build();

        var promptExim = new PromptEximDto();
        promptExim.setId("prompts/public/from/PROMPT 1__1.0.0");
        promptExim.setDescription("Test description");
        promptExim.setContent("Test content");
        var promptsExim = new PromptsEximDto();
        promptsExim.setPrompts(List.of(promptExim));

        var updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(path)
                .rules(rules)
                .build();
        doNothing().when(folderService).updatesRules(updateRulesRequest);

        // when
        var importResults = promptEximService.importPrompts(importPrompts, promptsExim);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var importResult = importResults.getImportResults().get(0);
        assertThat(importResult.getSourcePath()).isEqualTo("public/from/PROMPT 1__1.0.0");
        assertThat(importResult.getTargetPath()).isEqualTo("public/to/from/PROMPT 1__1.0.0");
        assertThat(importResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(importResult.getError()).isNull();

        var captor = ArgumentCaptor.forClass(CreatePrompt.class);
        verify(promptService).putPrompt(captor.capture(), eq(true), isNull());
        verify(folderService).updatesRules(updateRulesRequest);

        var prompt = captor.getValue();
        assertThat(prompt.getName()).isEqualTo("PROMPT 1");
        assertThat(prompt.getVersion()).isEqualTo("1.0.0");
        assertThat(prompt.getFolderId()).isEqualTo("public/to/from/");
        assertThat(prompt.getDescription()).isEqualTo("Test description");
        assertThat(prompt.getContent()).isEqualTo("Test content");
    }

    @Test
    @SneakyThrows
    void importPrompts_FlatImport() {
        // given
        var path = "public/to/";
        var rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        var rules = List.of(rule);
        var importPrompts = ImportResources.builder()
                .path(path)
                .flatImport(true)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .rules(rules)
                .build();

        var promptExim = new PromptEximDto();
        promptExim.setId("prompts/public/from/PROMPT 1__1.0.0");
        promptExim.setDescription("Test description");
        promptExim.setContent("Test content");
        var promptsExim = new PromptsEximDto();
        promptsExim.setPrompts(List.of(promptExim));

        var updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(path)
                .rules(rules)
                .build();
        doNothing().when(folderService).updatesRules(updateRulesRequest);

        // when
        var importResults = promptEximService.importPrompts(importPrompts, promptsExim);

        // then
        assertThat(importResults.getImportResults()).hasSize(1);
        var importResult = importResults.getImportResults().get(0);
        assertThat(importResult.getSourcePath()).isEqualTo("public/from/PROMPT 1__1.0.0");
        assertThat(importResult.getTargetPath()).isEqualTo("public/to/PROMPT 1__1.0.0");
        assertThat(importResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(importResult.getError()).isNull();

        var captor = ArgumentCaptor.forClass(CreatePrompt.class);
        verify(promptService).putPrompt(captor.capture(), eq(true), isNull());
        verify(folderService).updatesRules(updateRulesRequest);

        var prompt = captor.getValue();
        assertThat(prompt.getName()).isEqualTo("PROMPT 1");
        assertThat(prompt.getVersion()).isEqualTo("1.0.0");
        assertThat(prompt.getFolderId()).isEqualTo("public/to/");
        assertThat(prompt.getDescription()).isEqualTo("Test description");
        assertThat(prompt.getContent()).isEqualTo("Test content");
    }

    @Test
    @SneakyThrows
    void importPrompts_UniquenessConflict_ImportsRemainingItemsAndReturnsPerItemFailures() {
        // given
        var path = "public/test/";
        var importPrompts = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var okPrompt = new PromptEximDto();
        okPrompt.setId("prompts/public/from/OK__1.0.0");
        okPrompt.setDescription("ok");
        okPrompt.setContent("ok body");
        var conflictPrompt = new PromptEximDto();
        conflictPrompt.setId("prompts/public/from/BAD__1.0.0");
        conflictPrompt.setDescription("bad");
        conflictPrompt.setContent("bad body");
        var promptsExim = new PromptsEximDto();
        promptsExim.setPrompts(List.of(okPrompt, conflictPrompt));
        promptsExim.setFolders(List.of());

        when(validator.collectUniquenessConflicts(eq(importPrompts), eq(promptsExim)))
                .thenReturn(Map.of(
                        conflictPrompt.getId(),
                        "Duplicate prompt id: \"public/from/BAD__1.0.0\" appears 2 times in this import file."
                ));

        // when
        var importResults = promptEximService.importPrompts(importPrompts, promptsExim);

        // then
        assertThat(importResults.getImportResults()).hasSize(2);
        assertThat(importResults.getImportResults().get(0).getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);
        assertThat(importResults.getImportResults().get(1).getStatus()).isEqualTo(ImportResourcesStatus.FAILURE);
        assertThat(importResults.getImportResults().get(1).getError()).contains("Duplicate prompt id");

        verify(promptService).putPrompt(any(CreatePrompt.class), eq(true), isNull());
    }

}