package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.prompt.PromptService;
import com.epam.aidial.cfg.service.publication.PublicationService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FolderServiceTest {

    private ApplicationService applicationService;
    private ConversationService conversationService;
    private FileService fileService;
    private PromptService promptService;
    private PublicationService publicationService;
    private FolderService folderService;

    @BeforeEach
    void init() {
        applicationService = mock(ApplicationService.class);
        conversationService = mock(ConversationService.class);
        fileService = mock(FileService.class);
        promptService = mock(PromptService.class);
        publicationService = mock(PublicationService.class);
        Map<ResourceType, ResourceService> resourceServicesByResourceType = Map.of(
                ResourceType.APPLICATION, applicationService,
                ResourceType.CONVERSATION, conversationService,
                ResourceType.FILE, fileService,
                ResourceType.PROMPT, promptService
        );
        folderService = new FolderService(resourceServicesByResourceType, publicationService);
    }

    @Test
    void testGetFolders() {
        // given
        ResourceMetadataRequest request = ResourceMetadataRequest.builder().path("public/").build();
        FolderInfo folderInfoItem = FolderInfo.builder()
                .name("test")
                .bucket("public")
                .path("public/test/")
                .items(List.of())
                .build();

        FolderInfo folderInfo = FolderInfo.builder()
                .bucket("public")
                .path("public/")
                .items(List.of(folderInfoItem))
                .build();
        when(fileService.getFolders(request)).thenReturn(folderInfo);
        when(promptService.getFolders(request)).thenReturn(folderInfo);
        // when
        FolderInfo result = folderService.getFolders(request);
        // then
        Assertions.assertThat(result).isNotNull().satisfies(folder -> {
            Assertions.assertThat(folder.getBucket()).isEqualTo("public");
            Assertions.assertThat(folder.getPath()).isEqualTo("public/");
            Assertions.assertThat(folder.getItems()).hasSize(1);
            Assertions.assertThat(folder.getItems().get(0)).isNotNull().satisfies(item -> {
                Assertions.assertThat(item.getName()).isEqualTo("test");
                Assertions.assertThat(item.getBucket()).isEqualTo("public");
                Assertions.assertThat(item.getPath()).isEqualTo("public/test/");
                Assertions.assertThat(item.getItems()).isEmpty();
            });
        });
    }

    @Test
    void testGetRules() {
        // given
        String path = "public/test/";
        Rule rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        Map<String, List<Rule>> rules = Map.of("public/test/", List.of(rule));
        when(publicationService.getRules(path)).thenReturn(rules);
        // when
        Map<String, List<Rule>> result = folderService.getRules(path);
        // then
        Assertions.assertThat(result).isNotEmpty().isEqualTo(rules);
    }

    @Test
    void testUpdateRules() {
        // given
        Rule rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();
        UpdateRulesRequest request = UpdateRulesRequest.builder()
                .targetFolder("public/test/")
                .rules(List.of(rule))
                .build();
        when(publicationService.createPublication(any())).thenReturn("publications/publicationUrl");
        doNothing().when(publicationService).approvePublication("publicationUrl");
        // when
        folderService.updatesRules(request);
        // then
        verify(publicationService).createPublication(any());
        verify(publicationService).approvePublication("publicationUrl");
    }

    @Test
    void testUnpublishFolder() {
        // given
        String path = "public/test/";
        when(fileService.getResourceUrls(path)).thenReturn(Set.of("files/public/test/test.json"));
        when(publicationService.createPublication(any())).thenReturn("publications/publicationUrl");
        // when
        folderService.unpublishFolder(path);
        // then
        verify(applicationService).getResourceUrls(path);
        verify(conversationService).getResourceUrls(path);
        verify(fileService).getResourceUrls(path);
        verify(promptService).getResourceUrls(path);
        verify(publicationService).createPublication(any());
        verify(publicationService).approvePublication("publicationUrl");
    }

}