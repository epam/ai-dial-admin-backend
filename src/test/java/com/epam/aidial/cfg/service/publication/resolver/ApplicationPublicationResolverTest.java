package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleDto;
import com.epam.aidial.cfg.client.dto.RuleFunctionDto;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapperImpl;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.PublicationStatus;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.service.ApplicationResourceService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationPublicationResolverTest {

    @Mock
    private PublicationResourceUrlResolver publicationResourceUrlResolver;
    @Mock
    private ApplicationResourceService applicationService;
    @Spy
    private PublicationClientMapperImpl publicationClientMapper;

    @InjectMocks
    private ApplicationPublicationResolver applicationPublicationResolver;

    @BeforeEach
    void setUp() {
        applicationPublicationResolver = new ApplicationPublicationResolver(publicationClientMapper, applicationService);
        ReflectionTestUtils.setField(applicationPublicationResolver, "resolver", publicationResourceUrlResolver);
    }

    @Test
    void resolvePublicationShouldReturnCorrectApplicationPublication() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;
        var applicationsPrefix = "applications/";
        var applicationName = "testApplication";

        var reviewFolder = "reviewFolder/";
        var reviewApplicationPath = reviewFolder + applicationName;

        var targetFolder = "targetFolder/";
        var targetApplicationPath = targetFolder + applicationName;

        var sourceFolder = "sourceFolder/";
        var sourceFolderPath = sourceFolder + applicationName;

        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(applicationsPrefix + targetApplicationPath);
        publicationResource.setReviewUrl(applicationsPrefix + reviewApplicationPath);
        publicationResource.setSourceUrl(applicationsPrefix + sourceFolderPath);

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
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.APPLICATION));
        publicationDto.setRules(List.of(ruleDto));

        var applicationResource = new ApplicationResource();
        applicationResource.setPath(reviewApplicationPath);
        applicationResource.setFolderId(reviewFolder);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(applicationsPrefix + reviewApplicationPath);
        when(applicationService.getApplicationResource(reviewApplicationPath)).thenReturn(applicationResource);

        // when
        var result = applicationPublicationResolver.resolvePublication(publicationDto);

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
        assertThat(resource).isInstanceOf(ApplicationPublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var applicationPublicationResource = (ApplicationPublicationResource) resource;
        assertThat(applicationPublicationResource.getApplicationResource()).isNotNull();

        var actualApplicationResource = applicationPublicationResource.getApplicationResource();
        assertThat(actualApplicationResource).isNotNull();
        assertThat(actualApplicationResource.getPath()).isEqualTo(reviewApplicationPath);
        assertThat(actualApplicationResource.getFolderId()).isEqualTo(reviewFolder);

        assertThat(result).isInstanceOf(ApplicationPublication.class);
        var applicationPublication = (ApplicationPublication) result;
        assertThat(applicationPublication.getFiles()).isEmpty();
    }

    @Test
    void resolvePublicationShouldReturnCorrectApplicationPublicationWithFiles() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;
        var applicationsPrefix = "applications/";
        var filesPrefix = "files/";
        var applicationName = "testApplication";
        var fileName = "testFile";

        var reviewFolder = "reviewFolder/";
        var reviewApplicationPath = reviewFolder + applicationName;

        var targetFolder = "targetFolder/";
        var targetApplicationPath = targetFolder + applicationName;

        var sourceFolder = "sourceFolder/";
        var sourceFolderPath = sourceFolder + applicationName;

        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(applicationsPrefix + targetApplicationPath);
        publicationResource.setReviewUrl(applicationsPrefix + reviewApplicationPath);
        publicationResource.setSourceUrl(applicationsPrefix + sourceFolderPath);

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
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE, ResourceTypeDto.APPLICATION));
        publicationDto.setRules(List.of(ruleDto));

        var applicationResource = new ApplicationResource();
        applicationResource.setPath(reviewApplicationPath);
        applicationResource.setFolderId(reviewFolder);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING))
                .thenReturn(applicationsPrefix + reviewApplicationPath);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING))
                .thenReturn(filesPrefix + reviewFolder + fileName);
        when(applicationService.getApplicationResource(reviewApplicationPath)).thenReturn(applicationResource);

        // when
        var result = applicationPublicationResolver.resolvePublication(publicationDto);

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
        assertThat(resource).isInstanceOf(ApplicationPublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var applicationPublicationResource = (ApplicationPublicationResource) resource;
        assertThat(applicationPublicationResource.getApplicationResource()).isNotNull();

        var actualApplicationResource = applicationPublicationResource.getApplicationResource();
        assertThat(actualApplicationResource).isNotNull();
        assertThat(actualApplicationResource.getPath()).isEqualTo(reviewApplicationPath);
        assertThat(actualApplicationResource.getFolderId()).isEqualTo(reviewFolder);

        assertThat(result).isInstanceOf(ApplicationPublication.class);
        var applicationPublication = (ApplicationPublication) result;
        assertThat(applicationPublication.getFiles()).containsExactly(reviewFolder + fileName);
    }

    @Test
    void resolvePublicationShouldThrowExceptionWhenNotApplicableResourceTypesPresent() {
        // given
        var publicationDto = new PublicationDto();
        publicationDto.setUrl("URL");
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE, ResourceTypeDto.APPLICATION, ResourceTypeDto.PROMPT));

        // then
        assertThatThrownBy(() -> applicationPublicationResolver.resolvePublication(publicationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Found not applicable resource types: [PROMPT].")
                .hasMessageContaining("Publication: PublicationDto")
                .hasMessageContaining("resourceTypes=[FILE, APPLICATION, PROMPT]");
    }

}
