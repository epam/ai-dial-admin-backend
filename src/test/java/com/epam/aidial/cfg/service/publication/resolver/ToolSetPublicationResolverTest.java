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
import com.epam.aidial.cfg.model.PublicationMissingResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.PublicationStatus;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.model.ToolSetPublication;
import com.epam.aidial.cfg.model.ToolSetPublicationResource;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.service.ToolSetResourceService;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolSetPublicationResolverTest {
    private static final String PUBLICATION_PATH = "testPublication";
    private static final String FULL_PATH = "publications/" + PUBLICATION_PATH;
    private static final String TOOL_SET_PREFIX = "toolsets/";
    private static final String TOOL_SET_NAME = "testToolSet";
    private static final String REVIEW_FOLDER = "reviewFolder/";
    private static final String REVIEW_TOOL_SET_PATH = REVIEW_FOLDER + TOOL_SET_NAME;
    private static final String TARGET_FOLDER = "targetFolder/";
    private static final String TARGET_TOOL_SET_PATH = TARGET_FOLDER + TOOL_SET_NAME;
    private static final String SOURCE_FOLDER = "sourceFolder/";
    private static final String SOURCE_FOLDER_PATH = SOURCE_FOLDER + TOOL_SET_NAME;
    private static final String FILES_PREFIX = "files/";
    private static final String FILE_NAME = "testFile";

    @Mock
    private PublicationResourceUrlResolver publicationResourceUrlResolver;
    @Mock
    private ToolSetResourceService toolSetResourceService;
    @Mock
    private FilePublicationResolver filePublicationResolver;
    @Spy
    private PublicationClientMapperImpl publicationClientMapper;

    @InjectMocks
    private ToolSetPublicationResolver toolSetPublicationResolver;

    @Test
    void resolvePublicationShouldReturnCorrectToolSetPublication() {
        // given
        var publicationResource = createPublicationResourceDto();
        var ruleDto = createRuleDto();
        var publicationDto = createPublicationDto(List.of(publicationResource),
                List.of(ResourceTypeDto.TOOL_SET),
                List.of(ruleDto));
        var toolSetResource = createToolSetResource();

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(TOOL_SET_PREFIX + REVIEW_TOOL_SET_PATH);

        when(toolSetResourceService.getToolSetResource(REVIEW_TOOL_SET_PATH)).thenReturn(toolSetResource);

        // when
        var result = toolSetPublicationResolver.resolvePublication(publicationDto);

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
        assertThat(resource).isInstanceOf(ToolSetPublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var toolsetPublicationResource = (ToolSetPublicationResource) resource;
        assertThat(toolsetPublicationResource.getToolSetResource()).isNotNull();

        var actualToolsetPublicationResource = toolsetPublicationResource.getToolSetResource();
        assertThat(actualToolsetPublicationResource).isNotNull();
        assertThat(actualToolsetPublicationResource.getPath()).isEqualTo(REVIEW_TOOL_SET_PATH);
        assertThat(actualToolsetPublicationResource.getFolderId()).isEqualTo(REVIEW_FOLDER);

        assertThat(result).isInstanceOf(ToolSetPublication.class);
        var toolSetPublication = (ToolSetPublication) result;
        assertThat(toolSetPublication.getFiles()).isEmpty();
    }

    @Test
    void resolvePublicationShouldReturnCorrectToolSetPublicationWithFiles() {
        // given
        var publicationResource = createPublicationResourceDto();
        var filePublicationResource = createFilePublicationResourceDto();
        var ruleDto = createRuleDto();
        var publicationDto = createPublicationDto(List.of(publicationResource, filePublicationResource),
                List.of(ResourceTypeDto.FILE, ResourceTypeDto.TOOL_SET),
                List.of(ruleDto));
        var toolSetResource = createToolSetResource();

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(TOOL_SET_PREFIX + REVIEW_TOOL_SET_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING))
                .thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(toolSetResourceService.getToolSetResource(REVIEW_TOOL_SET_PATH)).thenReturn(toolSetResource);
        when(filePublicationResolver.resolveFileResourcePaths(anyList(), anyList())).thenReturn(List.of(REVIEW_FOLDER + FILE_NAME));

        // when
        var result = toolSetPublicationResolver.resolvePublication(publicationDto);

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
        assertThat(resource).isInstanceOf(ToolSetPublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var toolSetPublicationResource = (ToolSetPublicationResource) resource;
        assertThat(toolSetPublicationResource.getToolSetResource()).isNotNull();

        var actualToolSetResource = toolSetPublicationResource.getToolSetResource();
        assertThat(actualToolSetResource).isNotNull();
        assertThat(actualToolSetResource.getPath()).isEqualTo(REVIEW_TOOL_SET_PATH);
        assertThat(actualToolSetResource.getFolderId()).isEqualTo(REVIEW_FOLDER);

        assertThat(result).isInstanceOf(ToolSetPublication.class);
        var toolSetPublication = (ToolSetPublication) result;
        assertThat(toolSetPublication.getFiles()).containsExactly(REVIEW_FOLDER + FILE_NAME);
    }

    @Test
    void resolvePublicationShouldThrowExceptionWhenNotToolSetResourceTypesPresent() {
        // given
        var publicationDto = new PublicationDto();
        publicationDto.setUrl("URL");
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE, ResourceTypeDto.TOOL_SET, ResourceTypeDto.PROMPT));

        // then
        assertThatThrownBy(() -> toolSetPublicationResolver.resolvePublication(publicationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Found not applicable resource types: [PROMPT].")
                .hasMessageContaining("Publication: PublicationDto")
                .hasMessageContaining("resourceTypes=[FILE, TOOL_SET, PROMPT]");
    }

    @Test
    void resolvePublicationShouldReturnSingleMissingResourceWhenSomeResourceNotPresent() {
        // given
        var publicationResource = createPublicationResourceDto();
        var ruleDto = createRuleDto();
        var publicationDto = createPublicationDto(List.of(publicationResource),
                List.of(ResourceTypeDto.TOOL_SET),
                List.of(ruleDto));
        var toolSetResource = createToolSetResource();

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(TOOL_SET_PREFIX + REVIEW_TOOL_SET_PATH);

        when(toolSetResourceService.getToolSetResource(REVIEW_TOOL_SET_PATH)).thenThrow(ResourceNotFoundException.class);

        // when
        var result = toolSetPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getMissingResources()).hasSize(1);
        var missingResource = result.getMissingResources().get(0);
        assertThat(missingResource.getResourceType()).isEqualTo(ResourceType.TOOL_SET);
        assertThat(missingResource.getMessage()).isEqualTo("ToolSet not found");
        assertThat(missingResource.getPath()).isEqualTo("reviewFolder/testToolSet");
    }

    @Test
    void resolvePublicationShouldReturnManyMissingResourceWhenSomeResourceNotPresent() {
        // given
        var publicationResource = createPublicationResourceDto();
        var ruleDto = createRuleDto();
        var publicationDto = createPublicationDto(List.of(publicationResource),
                List.of(ResourceTypeDto.TOOL_SET),
                List.of(ruleDto));
        var toolSetResource = createToolSetResource();

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(TOOL_SET_PREFIX + REVIEW_TOOL_SET_PATH);

        when(toolSetResourceService.getToolSetResource(REVIEW_TOOL_SET_PATH)).thenThrow(ResourceNotFoundException.class);
        doAnswer(invocation -> {
            List<PublicationMissingResource> missing = invocation.getArgument(1);
            missing.add(new PublicationMissingResource(ResourceType.FILE, "/missing/file", "File not found"));
            return List.of(REVIEW_FOLDER + FILE_NAME);
        }).when(filePublicationResolver)
                .resolveFileResourcePaths(anyList(), anyList());

        // when
        var result = (ToolSetPublication) toolSetPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getMissingResources()).hasSize(2);
        assertThat(result.getFiles()).hasSize(1);
        var missingResource1 = result.getMissingResources().get(0);
        assertThat(missingResource1.getResourceType()).isEqualTo(ResourceType.TOOL_SET);
        assertThat(missingResource1.getMessage()).isEqualTo("ToolSet not found");
        assertThat(missingResource1.getPath()).isEqualTo("reviewFolder/testToolSet");
        var missingResource2 = result.getMissingResources().get(1);
        assertThat(missingResource2.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(missingResource2.getMessage()).isEqualTo("File not found");
        assertThat(missingResource2.getPath()).isEqualTo("/missing/file");
    }

    private PublicationResourceDto createPublicationResourceDto() {
        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(TOOL_SET_PREFIX + TARGET_TOOL_SET_PATH);
        publicationResource.setReviewUrl(TOOL_SET_PREFIX + REVIEW_TOOL_SET_PATH);
        publicationResource.setSourceUrl(TOOL_SET_PREFIX + SOURCE_FOLDER_PATH);
        return publicationResource;
    }

    private PublicationResourceDto createFilePublicationResourceDto() {
        var filePublicationResource = new PublicationResourceDto();
        filePublicationResource.setAction(PublicationResourceActionDto.ADD);
        filePublicationResource.setTargetUrl(FILES_PREFIX + TARGET_FOLDER + FILE_NAME);
        filePublicationResource.setReviewUrl(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        filePublicationResource.setSourceUrl(FILES_PREFIX + SOURCE_FOLDER + FILE_NAME);
        return filePublicationResource;
    }

    private RuleDto createRuleDto() {
        var ruleDto = new RuleDto();
        ruleDto.setSource("role");
        ruleDto.setFunction(RuleFunctionDto.EQUAL);
        ruleDto.setTargets(List.of("admin"));
        return ruleDto;
    }

    private ToolSetResource createToolSetResource() {
        var toolSetResource = new ToolSetResource();
        toolSetResource.setPath(REVIEW_TOOL_SET_PATH);
        toolSetResource.setFolderId(REVIEW_FOLDER);
        return toolSetResource;
    }

    private PublicationDto createPublicationDto(List<PublicationResourceDto> publicationResources,
                                                List<ResourceTypeDto> resourceTypes,
                                                List<RuleDto> rules) {
        var publicationDto = new PublicationDto();
        publicationDto.setUrl(FULL_PATH);
        publicationDto.setName("Test Publication");
        publicationDto.setAuthor("Author Name");
        publicationDto.setCreatedAt(100);
        publicationDto.setTargetFolder(TARGET_FOLDER);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResources(publicationResources);
        publicationDto.setResourceTypes(resourceTypes);
        publicationDto.setRules(rules);
        return publicationDto;
    }
}