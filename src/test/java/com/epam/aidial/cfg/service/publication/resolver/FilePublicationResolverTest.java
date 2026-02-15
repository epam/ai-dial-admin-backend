package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleDto;
import com.epam.aidial.cfg.client.dto.RuleFunctionDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapperImpl;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapperImpl;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.FilePublicationResource;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.PublicationStatus;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.service.FileService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilePublicationResolverTest {

    @Mock
    private PublicationResourceUrlResolver publicationResourceUrlResolver;
    @Mock
    private FileService fileService;
    @Spy
    private FileClientMapperImpl fileClientMapper;
    @Spy
    private PublicationClientMapperImpl publicationClientMapper;

    @InjectMocks
    private FilePublicationResolver filePublicationResolver;

    @Test
    void resolvePublicationShouldReturnCorrectFilePublication() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;
        var filePrefix = "files/";
        var fileName = "testFile";

        var reviewFolder = "reviewFolder/";
        var reviewFilePath = reviewFolder + fileName;

        var targetFolder = "targetFolder/";
        var targetFilePath = targetFolder + fileName;

        var sourceFolder = "sourceFolder/";
        var sourceFolderPath = sourceFolder + fileName;

        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(filePrefix + targetFilePath);
        publicationResource.setReviewUrl(filePrefix + reviewFilePath);
        publicationResource.setSourceUrl(filePrefix + sourceFolderPath);

        var ruleDto = new RuleDto();
        ruleDto.setSource("role");
        ruleDto.setFunction(RuleFunctionDto.EQUAL);
        ruleDto.setTargets(List.of("user"));

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setName("Test Publication");
        publicationDto.setAuthor("Author Name");
        publicationDto.setCreatedAt(100);
        publicationDto.setTargetFolder(targetFolder);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResources(List.of(publicationResource));
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE));
        publicationDto.setRules(List.of(ruleDto));

        var fileNodeInfo = FileNodeInfo.builder()
                .path(reviewFilePath)
                .folderId(reviewFolder)
                .name(fileName)
                .nodeType(NodeType.ITEM)
                .build();

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(filePrefix + reviewFilePath);
        when(fileService.getAll(any())).thenReturn(fileNodeInfo);
        when(fileService.fileExists(anyString())).thenReturn(false);

        // when
        var result = filePublicationResolver.resolvePublication(publicationDto);

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
        assertThat(rule.getTargets()).containsExactly("user");

        assertThat(result.getResources()).hasSize(1);
        var resource = result.getResources().get(0);
        assertThat(resource).isInstanceOf(FilePublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var fileResource = (FilePublicationResource) resource;
        assertThat(fileResource.getFile()).isNotNull();

        var actualFile = fileResource.getFile();
        assertThat(actualFile).isNotNull();
        assertThat(actualFile.getPath()).isEqualTo(reviewFilePath);
        assertThat(actualFile.getFolderId()).isEqualTo(reviewFolder);
    }

    @Test
    void resolvePublicationShouldThrowExceptionWhenNotApplicableResourceTypesPresent() {
        // given
        var publicationDto = new PublicationDto();
        publicationDto.setUrl("URL");
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE, ResourceTypeDto.APPLICATION, ResourceTypeDto.PROMPT));

        // then
        assertThatThrownBy(() -> filePublicationResolver.resolvePublication(publicationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Found not applicable resource types: [APPLICATION, PROMPT].")
                .hasMessageContaining("Publication: PublicationDto")
                .hasMessageContaining("resourceTypes=[FILE, APPLICATION, PROMPT]");
    }

    @Test
    void resolvePublicationShouldReturnMissingResourceWhenSomeResourceNotPresent() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;
        var filePrefix = "files/";
        var fileName = "testFile";

        var reviewFolder = "reviewFolder/";
        var reviewFilePath = reviewFolder + fileName;

        var targetFolder = "targetFolder/";
        var targetFilePath = targetFolder + fileName;

        var sourceFolder = "sourceFolder/";
        var sourceFolderPath = sourceFolder + fileName;

        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(filePrefix + targetFilePath);
        publicationResource.setReviewUrl(filePrefix + reviewFilePath);
        publicationResource.setSourceUrl(filePrefix + sourceFolderPath);

        var ruleDto = new RuleDto();
        ruleDto.setSource("role");
        ruleDto.setFunction(RuleFunctionDto.EQUAL);
        ruleDto.setTargets(List.of("user"));

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setName("Test Publication");
        publicationDto.setAuthor("Author Name");
        publicationDto.setCreatedAt(100);
        publicationDto.setTargetFolder(targetFolder);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResources(List.of(publicationResource));
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE));
        publicationDto.setRules(List.of(ruleDto));

        var fileNodeInfo = FileNodeInfo.builder()
                .path(reviewFilePath)
                .folderId(reviewFolder)
                .name(fileName)
                .nodeType(NodeType.ITEM)
                .build();

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(filePrefix + reviewFilePath);
        when(fileService.getAll(any())).thenThrow(ResourceNotFoundException.class);

        // when
        var result = filePublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getResourceIssues()).hasSize(1);
        var missingResource = result.getResourceIssues().get(0);
        assertThat(missingResource.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(missingResource.getMessage()).isEqualTo("File not found");
        assertThat(missingResource.getPath()).isEqualTo("reviewFolder/testFile");
    }

    @Test
    void resolvePublicationShouldReturnResourceIssueWhenSomeResourceIsPublished() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;
        var filePrefix = "files/";
        var fileName = "testFile";

        var reviewFolder = "reviewFolder/";
        var reviewFilePath = reviewFolder + fileName;

        var targetFolder = "targetFolder/";
        var targetFilePath = targetFolder + fileName;

        var sourceFolder = "sourceFolder/";
        var sourceFolderPath = sourceFolder + fileName;

        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(filePrefix + targetFilePath);
        publicationResource.setReviewUrl(filePrefix + reviewFilePath);
        publicationResource.setSourceUrl(filePrefix + sourceFolderPath);

        var ruleDto = new RuleDto();
        ruleDto.setSource("role");
        ruleDto.setFunction(RuleFunctionDto.EQUAL);
        ruleDto.setTargets(List.of("user"));

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setName("Test Publication");
        publicationDto.setAuthor("Author Name");
        publicationDto.setCreatedAt(100);
        publicationDto.setTargetFolder(targetFolder);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResources(List.of(publicationResource));
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE));
        publicationDto.setRules(List.of(ruleDto));

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(filePrefix + reviewFilePath);

        when(fileService.fileExists(anyString())).thenReturn(true);

        // when
        var result = filePublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getResourceIssues()).hasSize(1);
        var missingResource = result.getResourceIssues().get(0);
        assertThat(missingResource.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(missingResource.getMessage()).isEqualTo("Target file already exists");
        assertThat(missingResource.getPath()).isEqualTo("files/targetFolder/testFile");
    }

}