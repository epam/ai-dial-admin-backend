package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.ConversationClientMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ConversationEximDto;
import com.epam.aidial.cfg.dto.ConversationsEximDto;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationNodeInfo;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesStatus;
import com.epam.aidial.cfg.model.ModelResource;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.RuleFunction;
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
        ConversationClientMapperImpl.class,
        ConversationEximService.class
})
@TestPropertySource(properties = {
        "conversations.import.consecutiveErrorsThreshold=2"
})
class ConversationEximServiceTest {

    @MockitoBean
    private ConversationService conversationService;
    @MockitoBean
    private FolderService folderService;
    @MockitoBean
    private ResourceImportValidator validator;

    @Autowired
    private ConversationEximService conversationEximService;

    @Test
    @SneakyThrows
    void exportConversations_SinglePath() {
        var conversation = getConversation("1");
        var path = conversation.getPath();

        when(conversationService.getConversation(path)).thenReturn(conversation);

        var result = conversationEximService.exportConversations(List.of(path));

        assertThat(result).isNotNull();
        assertThat(result.getConversations()).hasSize(1);

        var exim = result.getConversations().get(0);
        assertThat(exim.getName()).isEqualTo("conversation1");
        assertThat(exim.getFolderId()).isEqualTo("public/");
        assertThat(exim.getPrompt()).isEqualTo("conversation description 1");
        assertThat(exim.getModel().getId()).isEqualTo("gpt-1");
    }

    @Test
    @SneakyThrows
    void exportConversations_MultiplePaths() {
        var conversation1 = getConversation("1");
        var conversation2 = getConversation("2");

        var path1 = conversation1.getPath();
        var path2 = conversation2.getPath();

        when(conversationService.getConversation(path1)).thenReturn(conversation1);
        when(conversationService.getConversation(path2)).thenReturn(conversation2);

        var result = conversationEximService.exportConversations(List.of(path1, path2));

        assertThat(result.getConversations()).hasSize(2);
        assertThat(result.getConversations().get(0).getName()).isEqualTo("conversation1");
        assertThat(result.getConversations().get(1).getName()).isEqualTo("conversation2");
    }

    @Test
    @SneakyThrows
    void exportConversations_FolderPath() {
        var folderPath = "public/folder1/folder2/folder3/";
        var storagePath = "public/folder1/folder2/folder3/folder4/conversation__0.0.1";

        var item = ConversationNodeInfo.builder()
                .nodeType(NodeType.ITEM)
                .path(storagePath)
                .build();
        var folder = ConversationNodeInfo.builder()
                .nodeType(NodeType.FOLDER)
                .items(List.of(item))
                .build();

        when(conversationService.getConversations(org.mockito.ArgumentMatchers.argThat(req ->
                folderPath.equals(req.getPath()) && req.isRecursive()))).thenReturn(folder);

        var conv = new Conversation();
        conv.setPath(storagePath);
        conv.setName("conversation");
        conv.setVersion("0.0.1");
        conv.setFolderId("public/folder1/folder2/folder3/folder4/");
        conv.setPrompt("prompt");
        conv.setModel(ModelResource.builder().id("model1").build());

        when(conversationService.getConversation(storagePath)).thenReturn(conv);

        var result = conversationEximService.exportConversations(List.of(folderPath));

        assertThat(result.getConversations()).hasSize(1);
        assertThat(result.getConversations().get(0).getName()).isEqualTo("conversation");
        assertThat(result.getConversations().get(0).getFolderId()).isEqualTo("public/folder3/folder4/");
        verify(conversationService).getConversation(storagePath);
    }

    @Test
    @SneakyThrows
    void exportConversations_FolderPath_excludesTechnicalFile() {
        var folderPath = "public/folder1/folder2/";
        var path = "public/folder1/folder2/test__0.0.1";
        var techPath = "public/folder1/folder2/" + ExportPathUtils.DIAL_FOLDER_FILE + "__0.0.1";

        var techItem = ConversationNodeInfo.builder()
                .nodeType(NodeType.ITEM)
                .path(techPath)
                .build();
        var item = ConversationNodeInfo.builder()
                .nodeType(NodeType.ITEM)
                .path(path)
                .build();
        var folder = ConversationNodeInfo.builder()
                .nodeType(NodeType.FOLDER)
                .items(List.of(techItem, item))
                .build();

        when(conversationService.getConversations(org.mockito.ArgumentMatchers.argThat(req ->
                folderPath.equals(req.getPath()) && req.isRecursive()))).thenReturn(folder);

        var conv = new Conversation();
        conv.setPath(path);
        conv.setName("test");
        conv.setVersion("0.0.1");
        conv.setFolderId("public/folder1/folder2/");
        conv.setPrompt("d");

        when(conversationService.getConversation(path)).thenReturn(conv);

        var result = conversationEximService.exportConversations(List.of(folderPath));

        assertThat(result.getConversations()).hasSize(1);
        assertThat(result.getConversations().get(0).getName()).isEqualTo("test");
        assertThat(result.getConversations().get(0).getFolderId()).isEqualTo("public/folder2/");
        verify(conversationService).getConversation(path);
    }

    @Test
    @SneakyThrows
    void exportConversations_EmptyPaths_ReturnsEmptyResult() {
        var result = conversationEximService.exportConversations(Collections.emptyList());

        assertThat(result.getConversations()).isEmpty();

        verifyNoInteractions(conversationService);
    }

    @Test
    void exportConversations_ServiceThrowsException_PropagatesException() {
        var path = "public/folder1/conversation1__0.0.1";
        var exception = new RuntimeException("Test exception");

        when(conversationService.getConversation(anyString())).thenThrow(exception);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> conversationEximService.exportConversations(List.of(path)));

        assertThat(thrown.getCause()).isEqualTo(exception);
    }

    @Test
    @SneakyThrows
    void importConversations_NotFlatImport() {
        var path = "public/to/";
        var rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        var rules = List.of(rule);
        var importResources = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .rules(rules)
                .build();

        var exim = getConversationEximDto("1");
        var conversationsExim = new ConversationsEximDto();
        conversationsExim.setConversations(List.of(exim));

        var updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(path)
                .rules(rules)
                .build();
        doNothing().when(folderService).updatesRules(updateRulesRequest);

        var importResults = conversationEximService.importConversations(importResources, conversationsExim);

        assertThat(importResults.getImportResults()).hasSize(1);
        var importResult = importResults.getImportResults().get(0);
        assertThat(importResult.getSourcePath()).isEqualTo("public/folder1/conversation1__0.0.1");
        assertThat(importResult.getTargetPath()).isEqualTo("public/to/folder1/conversation1__0.0.1");
        assertThat(importResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);

        var captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationService).putConversation(captor.capture(), eq(true), isNull());
        verify(folderService).updatesRules(updateRulesRequest);

        var captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo("conversation1");
        assertThat(captured.getVersion()).isEqualTo("0.0.1");
        assertThat(captured.getFolderId()).isEqualTo("public/to/folder1/");
        assertThat(captured.getPrompt()).isEqualTo("conversation description 1");
    }

    @Test
    @SneakyThrows
    void importConversations_FlatImport() {
        var path = "public/to/";
        var rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        var rules = List.of(rule);
        var importResources = ImportResources.builder()
                .path(path)
                .flatImport(true)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .rules(rules)
                .build();

        var exim = getConversationEximDto("1");
        var conversationsExim = new ConversationsEximDto();
        conversationsExim.setConversations(List.of(exim));

        var updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(path)
                .rules(rules)
                .build();
        doNothing().when(folderService).updatesRules(updateRulesRequest);

        var importResults = conversationEximService.importConversations(importResources, conversationsExim);

        assertThat(importResults.getImportResults()).hasSize(1);
        var importResult = importResults.getImportResults().get(0);
        assertThat(importResult.getSourcePath()).isEqualTo("public/folder1/conversation1__0.0.1");
        assertThat(importResult.getTargetPath()).isEqualTo("public/to/conversation1__0.0.1");
        assertThat(importResult.getStatus()).isEqualTo(ImportResourcesStatus.SUCCESS);

        var captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationService).putConversation(captor.capture(), eq(true), isNull());

        var captured = captor.getValue();
        assertThat(captured.getFolderId()).isEqualTo("public/to/");
    }

    @Test
    @SneakyThrows
    void importConversations_ValidatorThrowsError_RethrowsError() {
        var importResources = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        var exim = getConversationEximDto("1");
        var conversationsExim = new ConversationsEximDto();
        conversationsExim.setConversations(List.of(exim));

        doThrow(new IllegalArgumentException("Validation error"))
                .when(validator).validateConversationImport(importResources, conversationsExim);

        var thrown = assertThrows(IllegalArgumentException.class,
                () -> conversationEximService.importConversations(importResources, conversationsExim));

        assertThat(thrown).hasMessage("Validation error");
        verifyNoInteractions(conversationService);
    }

    private Conversation getConversation(String suffix) {
        var model = ModelResource.builder().id("gpt-" + suffix).build();
        var conversation = new Conversation();
        conversation.setName("conversation" + suffix);
        conversation.setVersion(String.format("0.0.%s", suffix));
        conversation.setFolderId(String.format("public/folder%s/", suffix));
        conversation.setPath(String.format("%s%s__%s", conversation.getFolderId(), conversation.getName(), conversation.getVersion()));
        conversation.setPrompt(String.format("conversation description %s", suffix));
        conversation.setModel(model);
        return conversation;
    }

    private ConversationEximDto getConversationEximDto(String suffix) {
        return ConversationEximDto.builder()
                .name("conversation" + suffix)
                .version(String.format("0.0.%s", suffix))
                .folderId(String.format("public/folder%s/", suffix))
                .prompt(String.format("conversation description %s", suffix))
                .build();
    }
}