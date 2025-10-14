package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleDto;
import com.epam.aidial.cfg.client.dto.RuleFunctionDto;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapperImpl;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationPublication;
import com.epam.aidial.cfg.model.ConversationPublicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.PublicationStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationPublicationResolverTest {

    @Mock
    private PublicationResourceUrlResolver publicationResourceUrlResolver;
    @Mock
    private ConversationService conversationService;
    @Spy
    private PublicationClientMapperImpl publicationClientMapper;

    @InjectMocks
    private ConversationPublicationResolver conversationPublicationResolver;

    @Test
    void resolvePublicationShouldReturnCorrectConversationPublication() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;
        var conversationPrefix = "conversations/";
        var conversationName = "testConversation";

        var reviewFolder = "reviewFolder/";
        var reviewConversationPath = reviewFolder + conversationName;

        var targetFolder = "targetFolder/";
        var targetConversationPath = targetFolder + conversationName;

        var sourceFolder = "sourceFolder/";
        var sourceFolderPath = sourceFolder + conversationName;

        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(conversationPrefix + targetConversationPath);
        publicationResource.setReviewUrl(conversationPrefix + reviewConversationPath);
        publicationResource.setSourceUrl(conversationPrefix + sourceFolderPath);

        var ruleDto = new RuleDto();
        ruleDto.setSource("role");
        ruleDto.setFunction(RuleFunctionDto.EQUAL);
        ruleDto.setTargets(List.of("admin"));

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setName("Test Publication");
        publicationDto.setAuthor("Author Name");
        publicationDto.setCreatedAt(100);
        publicationDto.setTargetFolder(targetFolder);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResources(List.of(publicationResource));
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.CONVERSATION));
        publicationDto.setRules(List.of(ruleDto));

        var conversation = new Conversation();
        conversation.setPath(reviewConversationPath);
        conversation.setFolderId(reviewFolder);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(conversationPrefix + reviewConversationPath);
        when(conversationService.getConversation(reviewConversationPath)).thenReturn(conversation);

        // when
        var result = conversationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPath()).isEqualTo(publicationPath);
        assertThat(result.getRequestName()).isEqualTo("Test Publication");
        assertThat(result.getAuthor()).isEqualTo("Author Name");
        assertThat(result.getStatus()).isEqualTo(PublicationStatus.PENDING);
        assertThat(result.getFolderId()).isEqualTo(targetFolder);

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
        assertThat(actualConversationResource.getPath()).isEqualTo(reviewConversationPath);
        assertThat(actualConversationResource.getFolderId()).isEqualTo(reviewFolder);

        assertThat(result).isInstanceOf(ConversationPublication.class);
        var conversationPublication = (ConversationPublication) result;
        assertThat(conversationPublication.getFiles()).isEmpty();
    }

    @Test
    void resolvePublicationShouldReturnCorrectConversationPublicationWithFiles() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;
        var conversationPrefix = "conversations/";
        var filesPrefix = "files/";
        var conversationName = "testConversation";
        var fileName = "testFile";

        var reviewFolder = "reviewFolder/";
        var reviewConversationPath = reviewFolder + conversationName;

        var targetFolder = "targetFolder/";
        var targetConversationPath = targetFolder + conversationName;

        var sourceFolder = "sourceFolder/";
        var sourceFolderPath = sourceFolder + conversationName;

        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(conversationPrefix + targetConversationPath);
        publicationResource.setReviewUrl(conversationPrefix + reviewConversationPath);
        publicationResource.setSourceUrl(conversationPrefix + sourceFolderPath);

        var filePublicationResource = new PublicationResourceDto();
        filePublicationResource.setAction(PublicationResourceActionDto.ADD);
        filePublicationResource.setTargetUrl(filesPrefix + targetFolder + fileName);
        filePublicationResource.setReviewUrl(filesPrefix + reviewFolder + fileName);
        filePublicationResource.setSourceUrl(filesPrefix + sourceFolder + fileName);

        var ruleDto = new RuleDto();
        ruleDto.setSource("role");
        ruleDto.setFunction(RuleFunctionDto.EQUAL);
        ruleDto.setTargets(List.of("admin"));

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setName("Test Publication");
        publicationDto.setAuthor("Author Name");
        publicationDto.setCreatedAt(100);
        publicationDto.setTargetFolder(targetFolder);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResources(List.of(publicationResource, filePublicationResource));
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE, ResourceTypeDto.CONVERSATION));
        publicationDto.setRules(List.of(ruleDto));

        var conversation = new Conversation();
        conversation.setPath(reviewConversationPath);
        conversation.setFolderId(reviewFolder);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(conversationPrefix + reviewConversationPath);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING))
                .thenReturn(filesPrefix + reviewFolder + fileName);
        when(conversationService.getConversation(reviewConversationPath)).thenReturn(conversation);

        // when
        var result = conversationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPath()).isEqualTo(publicationPath);
        assertThat(result.getRequestName()).isEqualTo("Test Publication");
        assertThat(result.getAuthor()).isEqualTo("Author Name");
        assertThat(result.getStatus()).isEqualTo(PublicationStatus.PENDING);
        assertThat(result.getFolderId()).isEqualTo(targetFolder);

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
        assertThat(actualConversation.getPath()).isEqualTo(reviewConversationPath);
        assertThat(actualConversation.getFolderId()).isEqualTo(reviewFolder);

        assertThat(result).isInstanceOf(ConversationPublication.class);
        var conversationPublication = (ConversationPublication) result;
        assertThat(conversationPublication.getFiles()).containsExactly(reviewFolder + fileName);
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

}
