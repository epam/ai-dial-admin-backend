package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleDto;
import com.epam.aidial.cfg.client.dto.RuleFunctionDto;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapperImpl;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptPublicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.PublicationStatus;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.service.prompt.PromptService;
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
class PromptPublicationResolverTest {

    @Mock
    private PublicationResourceUrlResolver publicationResourceUrlResolver;
    @Mock
    private PromptService promptService;
    @Spy
    private PublicationClientMapperImpl publicationClientMapper;

    @InjectMocks
    private PromptPublicationResolver promptPublicationResolver;

    @Test
    void resolvePublicationShouldReturnCorrectPromptPublication() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;
        var promptsPrefix = "prompts/";
        var promptName = "testPrompt";

        var reviewFolder = "reviewFolder/";
        var reviewPromptPath = reviewFolder + promptName;

        var targetFolder = "targetFolder/";
        var targetPromptPath = targetFolder + promptName;

        var sourceFolder = "sourceFolder/";
        var sourceFolderPath = sourceFolder + promptName;

        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(promptsPrefix + targetPromptPath);
        publicationResource.setReviewUrl(promptsPrefix + reviewPromptPath);
        publicationResource.setSourceUrl(promptsPrefix + sourceFolderPath);

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
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.PROMPT));
        publicationDto.setRules(List.of(ruleDto));

        var prompt = new Prompt();
        prompt.setPath(reviewPromptPath);
        prompt.setFolderId(reviewFolder);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(promptsPrefix + reviewPromptPath);
        when(promptService.getPrompt(reviewPromptPath)).thenReturn(prompt);

        // when
        var result = promptPublicationResolver.resolvePublication(publicationDto);

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
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);
        assertThat(resource).isInstanceOf(PromptPublicationResource.class);

        var promptResource = (PromptPublicationResource) resource;
        assertThat(promptResource.getPrompt()).isNotNull();

        var actualPrompt = promptResource.getPrompt();
        assertThat(actualPrompt).isNotNull();
        assertThat(actualPrompt.getPath()).isEqualTo(reviewPromptPath);
        assertThat(actualPrompt.getFolderId()).isEqualTo(reviewFolder);
    }

    @Test
    void resolvePublicationShouldThrowExceptionWhenNotApplicableResourceTypesPresent() {
        // given
        var publicationDto = new PublicationDto();
        publicationDto.setUrl("URL");
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE, ResourceTypeDto.APPLICATION, ResourceTypeDto.PROMPT));

        // then
        assertThatThrownBy(() -> promptPublicationResolver.resolvePublication(publicationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Found not applicable resource types: [APPLICATION].")
                .hasMessageContaining("Publication: PublicationDto")
                .hasMessageContaining("resourceTypes=[FILE, APPLICATION, PROMPT]");
    }

}