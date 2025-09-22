package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.exception.FolderAlreadyExistsException;
import com.epam.aidial.cfg.exception.FolderNotFoundException;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveFolderRequest;
import com.epam.aidial.cfg.model.MoveResource;
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

    private ApplicationResourceService applicationService;
    private ConversationService conversationService;
    private FileService fileService;
    private PromptService promptService;
    private PublicationService publicationService;
    private FolderService folderService;

    @BeforeEach
    void init() {
        applicationService = mock(ApplicationResourceService.class);
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

    @Test
    void testMoveFolder() {
        // given
        String oldPath = "public/old";
        String newPath = "public/new1/new2";

        MoveFolderRequest moveFolderRequest = MoveFolderRequest.builder()
                .oldPath(oldPath)
                .newPath(newPath)
                .resourceTypes(List.of(ResourceType.PROMPT))
                .build();
        ResourceMetadataRequest oldResourceMetadataRequest = ResourceMetadataRequest.builder()
                .path(oldPath)
                .build();
        ResourceMetadataRequest newResourceMetadataRequest = ResourceMetadataRequest.builder()
                .path(newPath)
                .build();

        List<Rule> rules = List.of(Rule.builder().build());

        CreatePublication createPublication = CreatePublication.builder()
                .targetFolder(newPath)
                .rules(rules)
                .build();

        String prompt1OldPath = "prompts/public/old/prompt_1";
        String prompt2OldPath = "prompts/public/old/prompt_2";
        String prompt1NewPath = "prompts/public/new1/new2/prompt_1";
        String prompt2NewPath = "prompts/public/new1/new2/prompt_2";

        MoveResource movePrompt1 = MoveResource.builder().sourceUrl(prompt1OldPath).destinationUrl(prompt1NewPath).build();
        MoveResource movePrompt2 = MoveResource.builder().sourceUrl(prompt2OldPath).destinationUrl(prompt2NewPath).build();

        when(promptService.getFolders(oldResourceMetadataRequest)).thenReturn(FolderInfo.builder().build());

        when(applicationService.getFolders(newResourceMetadataRequest)).thenReturn(null);
        when(conversationService.getFolders(newResourceMetadataRequest)).thenReturn(null);
        when(fileService.getFolders(newResourceMetadataRequest)).thenReturn(null);
        when(promptService.getFolders(newResourceMetadataRequest)).thenReturn(null);

        when(publicationService.getRules(oldPath)).thenReturn(Map.of(oldPath, rules));
        when(publicationService.createPublication(createPublication)).thenReturn("publications/pub");
        doNothing().when(publicationService).approvePublication("pub");

        when(promptService.getResourceUrls(oldPath)).thenReturn(Set.of(prompt1OldPath, prompt2OldPath));
        doNothing().when(promptService).move(movePrompt1);
        doNothing().when(promptService).move(movePrompt2);

        // when
        folderService.moveFolder(moveFolderRequest);

        // then
        verify(promptService).getFolders(oldResourceMetadataRequest);
        verify(applicationService).getFolders(newResourceMetadataRequest);
        verify(conversationService).getFolders(newResourceMetadataRequest);
        verify(fileService).getFolders(newResourceMetadataRequest);
        verify(promptService).getFolders(newResourceMetadataRequest);
        verify(publicationService).getRules(oldPath);
        verify(publicationService).createPublication(createPublication);
        verify(publicationService).approvePublication("pub");
        verify(promptService).getResourceUrls(oldPath);
        verify(promptService).move(movePrompt1);
        verify(promptService).move(movePrompt2);
    }

    @Test
    void testMoveFolder_oldFolderDoesNotExist_exceptionIsThrown() {
        // given
        String oldPath = "public/old";
        String newPath = "public/new1/new2";

        MoveFolderRequest moveFolderRequest = MoveFolderRequest.builder()
                .oldPath(oldPath)
                .newPath(newPath)
                .resourceTypes(List.of(ResourceType.PROMPT))
                .build();
        ResourceMetadataRequest oldResourceMetadataRequest = ResourceMetadataRequest.builder()
                .path(oldPath)
                .build();

        when(promptService.getFolders(oldResourceMetadataRequest)).thenReturn(null);
        when(promptService.getResourceType()).thenReturn(ResourceType.PROMPT);

        // then
        Assertions.assertThatThrownBy(() -> folderService.moveFolder(moveFolderRequest))
                .isInstanceOf(FolderNotFoundException.class)
                .hasMessage("Folder: public/old does not exist in PROMPT resources");
    }

    @Test
    void testMoveFolder_newFolderAlreadyExists_exceptionIsThrown() {
        // given
        String oldPath = "public/old";
        String newPath = "public/new1/new2";

        MoveFolderRequest moveFolderRequest = MoveFolderRequest.builder()
                .oldPath(oldPath)
                .newPath(newPath)
                .resourceTypes(List.of(ResourceType.PROMPT))
                .build();
        ResourceMetadataRequest oldResourceMetadataRequest = ResourceMetadataRequest.builder()
                .path(oldPath)
                .build();
        ResourceMetadataRequest newResourceMetadataRequest = ResourceMetadataRequest.builder()
                .path(newPath)
                .build();

        when(promptService.getFolders(oldResourceMetadataRequest)).thenReturn(FolderInfo.builder().build());

        when(applicationService.getFolders(newResourceMetadataRequest)).thenReturn(FolderInfo.builder().build());
        when(conversationService.getFolders(newResourceMetadataRequest)).thenReturn(null);
        when(fileService.getFolders(newResourceMetadataRequest)).thenReturn(null);
        when(promptService.getFolders(newResourceMetadataRequest)).thenReturn(null);

        when(applicationService.getResourceType()).thenReturn(ResourceType.APPLICATION);

        // then
        Assertions.assertThatThrownBy(() -> folderService.moveFolder(moveFolderRequest))
                .isInstanceOf(FolderAlreadyExistsException.class)
                .hasMessage("Folder: public/new1/new2 already exists in APPLICATION resources");
    }

}