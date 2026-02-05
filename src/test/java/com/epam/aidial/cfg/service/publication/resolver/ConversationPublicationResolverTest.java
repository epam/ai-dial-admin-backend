package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleDto;
import com.epam.aidial.cfg.client.dto.RuleFunctionDto;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapperImpl;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationPublication;
import com.epam.aidial.cfg.model.ConversationPublicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.PublicationResourceIssue;
import com.epam.aidial.cfg.model.PublicationStatus;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.service.ConversationService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationPublicationResolverTest {
    private static final String PUBLICATION_PATH = "testPublication";
    private static final String FULL_PATH = "publications/" + PUBLICATION_PATH;
    private static final String CONVERSATION_PREFIX = "conversations/";
    private static final String FILES_PREFIX = "files/";
    private static final String CONVERSATION_NAME = "testConversation";
    private static final String FILE_NAME = "testFile";
    private static final String REVIEW_FOLDER = "reviewFolder/";
    private static final String REVIEW_CONVERSATION_PATH = REVIEW_FOLDER + CONVERSATION_NAME;
    private static final String TARGET_FOLDER = "targetFolder/";
    private static final String TARGET_CONVERSATION_PATH = TARGET_FOLDER + CONVERSATION_NAME;
    private static final String SOURCE_FOLDER = "sourceFolder/";
    private static final String SOURCE_FOLDER_PATH = SOURCE_FOLDER + CONVERSATION_NAME;

    @Mock
    private PublicationResourceUrlResolver publicationResourceUrlResolver;
    @Mock
    private ConversationService conversationService;
    @Mock
    private FilePublicationResolver filePublicationResolver;
    @Spy
    private PublicationClientMapperImpl publicationClientMapper;

    @InjectMocks
    private ConversationPublicationResolver conversationPublicationResolver;

    @Test
    void resolvePublicationShouldReturnCorrectConversationPublication() {
        // given
        var publicationResource = getPublicationResourceDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource), List.of(ResourceTypeDto.CONVERSATION),
                List.of(ruleDto));
        var conversation = new Conversation();
        conversation.setPath(REVIEW_CONVERSATION_PATH);
        conversation.setFolderId(REVIEW_FOLDER);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(CONVERSATION_PREFIX + REVIEW_CONVERSATION_PATH);
        when(conversationService.getConversation(REVIEW_CONVERSATION_PATH)).thenReturn(conversation);

        // when
        var result = conversationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPath()).isEqualTo(PUBLICATION_PATH);
        assertThat(result.getRequestName()).isEqualTo("Test Publication");
        assertThat(result.getAuthor()).isEqualTo("Author Name");
        assertThat(result.getStatus()).isEqualTo(PublicationStatus.PENDING);
        assertThat(result.getFolderId()).isEqualTo(TARGET_FOLDER);

        assertThat(result.getRules()).hasSize(1);
        var rule = result.getRules().get(0);
        assertThat(rule.getSource()).isEqualTo("role");
        assertThat(rule.getFunction()).isEqualTo(RuleFunction.EQUAL);
        assertThat(rule.getTargets()).containsExactly("admin");

        assertThat(result.getResources()).hasSize(1);
        var resource = result.getResources().get(0);
        assertThat(resource).isInstanceOf(ConversationPublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var conversationPublicationResource = (ConversationPublicationResource) resource;
        assertThat(conversationPublicationResource.getConversation()).isNotNull();

        var actualConversationResource = conversationPublicationResource.getConversation();
        assertThat(actualConversationResource).isNotNull();
        assertThat(actualConversationResource.getPath()).isEqualTo(REVIEW_CONVERSATION_PATH);
        assertThat(actualConversationResource.getFolderId()).isEqualTo(REVIEW_FOLDER);

        assertThat(result).isInstanceOf(ConversationPublication.class);
        var conversationPublication = (ConversationPublication) result;
        assertThat(conversationPublication.getFiles()).isEmpty();
    }

    @Test
    void resolvePublicationShouldReturnCorrectConversationPublicationWithFiles() {
        // given
        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        var filePublicationResource = getFilePublicationDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource, filePublicationResource),
                List.of(ResourceTypeDto.FILE, ResourceTypeDto.CONVERSATION), List.of(ruleDto));
        var conversation = new Conversation();
        conversation.setPath(REVIEW_CONVERSATION_PATH);
        conversation.setFolderId(REVIEW_FOLDER);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(CONVERSATION_PREFIX + REVIEW_CONVERSATION_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING))
                .thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(conversationService.getConversation(REVIEW_CONVERSATION_PATH)).thenReturn(conversation);
        when(filePublicationResolver.resolveFileResourcePaths(anyList(), anyList())).thenReturn(List.of(REVIEW_FOLDER + FILE_NAME));

        // when
        var result = conversationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPath()).isEqualTo(PUBLICATION_PATH);
        assertThat(result.getRequestName()).isEqualTo("Test Publication");
        assertThat(result.getAuthor()).isEqualTo("Author Name");
        assertThat(result.getStatus()).isEqualTo(PublicationStatus.PENDING);
        assertThat(result.getFolderId()).isEqualTo(TARGET_FOLDER);

        assertThat(result.getRules()).hasSize(1);
        var rule = result.getRules().get(0);
        assertThat(rule.getSource()).isEqualTo("role");
        assertThat(rule.getFunction()).isEqualTo(RuleFunction.EQUAL);
        assertThat(rule.getTargets()).containsExactly("admin");

        assertThat(result.getResources()).hasSize(1);
        var resource = result.getResources().get(0);
        assertThat(resource).isInstanceOf(ConversationPublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var conversationPublicationResource = (ConversationPublicationResource) resource;
        assertThat(conversationPublicationResource.getConversation()).isNotNull();

        var actualConversation = conversationPublicationResource.getConversation();
        assertThat(actualConversation).isNotNull();
        assertThat(actualConversation.getPath()).isEqualTo(REVIEW_CONVERSATION_PATH);
        assertThat(actualConversation.getFolderId()).isEqualTo(REVIEW_FOLDER);

        assertThat(result).isInstanceOf(ConversationPublication.class);
        var conversationPublication = (ConversationPublication) result;
        assertThat(conversationPublication.getFiles()).containsExactly(REVIEW_FOLDER + FILE_NAME);
    }

    @Test
    void resolvePublicationShouldThrowExceptionWhenNotApplicableResourceTypesPresent() {
        // given
        var publicationDto = new PublicationDto();
        publicationDto.setUrl("URL");
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE, ResourceTypeDto.CONVERSATION, ResourceTypeDto.PROMPT));

        // then
        assertThatThrownBy(() -> conversationPublicationResolver.resolvePublication(publicationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Found not applicable resource types: [PROMPT].")
                .hasMessageContaining("Publication: PublicationDto")
                .hasMessageContaining("resourceTypes=[FILE, CONVERSATION, PROMPT]");
    }

    @Test
    void resolvePublicationShouldReturnSingleMissingResourceWhenSomeResourceNotPresent() {
        // given
        var publicationResource = getPublicationResourceDto();
        var filePublicationResource = getFilePublicationDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource, filePublicationResource),
                List.of(ResourceTypeDto.FILE, ResourceTypeDto.CONVERSATION), List.of(ruleDto));
        var conversation = new Conversation();
        conversation.setPath(REVIEW_CONVERSATION_PATH);
        conversation.setFolderId(REVIEW_FOLDER);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(CONVERSATION_PREFIX + REVIEW_CONVERSATION_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING))
                .thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(conversationService.getConversation(REVIEW_CONVERSATION_PATH)).thenThrow(ResourceNotFoundException.class);
        when(filePublicationResolver.resolveFileResourcePaths(anyList(), anyList())).thenReturn(List.of(REVIEW_FOLDER + FILE_NAME));

        // when
        var result = conversationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getResourceIssues()).hasSize(1);
        var missingResource = result.getResourceIssues().get(0);
        assertThat(missingResource.getResourceType()).isEqualTo(ResourceType.CONVERSATION);
        assertThat(missingResource.getMessage()).isEqualTo("Conversation not found");
        assertThat(missingResource.getPath()).isEqualTo("reviewFolder/testConversation");
    }

    @Test
    void resolvePublicationShouldReturnManyMissingResourceWhenSomeResourceNotPresent() {
        // given
        var publicationResource = getPublicationResourceDto();
        var filePublicationResource = getFilePublicationDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource, filePublicationResource),
                List.of(ResourceTypeDto.FILE, ResourceTypeDto.CONVERSATION), List.of(ruleDto));
        var conversation = new Conversation();
        conversation.setPath(REVIEW_CONVERSATION_PATH);
        conversation.setFolderId(REVIEW_FOLDER);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(CONVERSATION_PREFIX + REVIEW_CONVERSATION_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING))
                .thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(conversationService.getConversation(REVIEW_CONVERSATION_PATH)).thenThrow(ResourceNotFoundException.class);
        doAnswer(invocation -> {
            List<PublicationResourceIssue> missing = invocation.getArgument(1);
            missing.add(new PublicationResourceIssue(ResourceType.FILE, "/missing/file", "File not found"));
            return List.of(REVIEW_FOLDER + FILE_NAME);

        }).when(filePublicationResolver)
                .resolveFileResourcePaths(anyList(), anyList());
        // when
        var result = conversationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getResourceIssues()).hasSize(2);
        var missingResource1 = result.getResourceIssues().get(0);
        assertThat(missingResource1.getResourceType()).isEqualTo(ResourceType.CONVERSATION);
        assertThat(missingResource1.getMessage()).isEqualTo("Conversation not found");
        assertThat(missingResource1.getPath()).isEqualTo("reviewFolder/testConversation");
        var missingResource2 = result.getResourceIssues().get(1);
        assertThat(missingResource2.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(missingResource2.getMessage()).isEqualTo("File not found");
        assertThat(missingResource2.getPath()).isEqualTo("/missing/file");
    }

    @Test
    void resolvePublicationShouldReturnResourceIssueWhenSomeResourceIsPublished() {
        // given
        var publicationResource = getPublicationResourceDto();
        var filePublicationResource = getFilePublicationDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource, filePublicationResource),
                List.of(ResourceTypeDto.FILE, ResourceTypeDto.CONVERSATION), List.of(ruleDto));
        var conversation = new Conversation();
        conversation.setPath(REVIEW_CONVERSATION_PATH);
        conversation.setFolderId(REVIEW_FOLDER);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(CONVERSATION_PREFIX + REVIEW_CONVERSATION_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING))
                .thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(conversationService.conversationExists(any())).thenReturn(true);
        doAnswer(invocation -> {
            List<PublicationResourceIssue> missing = invocation.getArgument(1);
            missing.add(new PublicationResourceIssue(ResourceType.FILE, "/missing/file", "File not found"));
            return List.of(REVIEW_FOLDER + FILE_NAME);

        }).when(filePublicationResolver)
                .resolveFileResourcePaths(anyList(), anyList());
        // when
        var result = conversationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getResourceIssues()).hasSize(2);
        var missingResource1 = result.getResourceIssues().get(0);
        assertThat(missingResource1.getResourceType()).isEqualTo(ResourceType.CONVERSATION);
        assertThat(missingResource1.getMessage()).isEqualTo("Target conversation already exists");
        assertThat(missingResource1.getPath()).isEqualTo("conversations/targetFolder/testConversation");
        var missingResource2 = result.getResourceIssues().get(1);
        assertThat(missingResource2.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(missingResource2.getMessage()).isEqualTo("File not found");
        assertThat(missingResource2.getPath()).isEqualTo("/missing/file");
    }

    private PublicationResourceDto getPublicationResourceDto() {
        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(CONVERSATION_PREFIX + TARGET_CONVERSATION_PATH);
        publicationResource.setReviewUrl(CONVERSATION_PREFIX + REVIEW_CONVERSATION_PATH);
        publicationResource.setSourceUrl(CONVERSATION_PREFIX + SOURCE_FOLDER_PATH);
        return publicationResource;
    }

    private RuleDto getRuleDto() {
        var ruleDto = new RuleDto();
        ruleDto.setSource("role");
        ruleDto.setFunction(RuleFunctionDto.EQUAL);
        ruleDto.setTargets(List.of("admin"));
        return ruleDto;
    }

    private PublicationDto getPublicationDto(List<PublicationResourceDto> publicationResources,
                                             List<ResourceTypeDto> types,
                                             List<RuleDto> rules) {
        var publicationDto = new PublicationDto();
        publicationDto.setUrl(FULL_PATH);
        publicationDto.setName("Test Publication");
        publicationDto.setAuthor("Author Name");
        publicationDto.setCreatedAt(100);
        publicationDto.setTargetFolder(TARGET_FOLDER);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResources(publicationResources);
        publicationDto.setResourceTypes(types);
        publicationDto.setRules(rules);
        return publicationDto;
    }

    private PublicationResourceDto getFilePublicationDto() {
        var filePublicationResource = new PublicationResourceDto();
        filePublicationResource.setAction(PublicationResourceActionDto.ADD);
        filePublicationResource.setTargetUrl(FILES_PREFIX + TARGET_FOLDER + FILE_NAME);
        filePublicationResource.setReviewUrl(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        filePublicationResource.setSourceUrl(FILES_PREFIX + SOURCE_FOLDER + FILE_NAME);
        return filePublicationResource;
    }
}