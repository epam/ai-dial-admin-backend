package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleDto;
import com.epam.aidial.cfg.client.dto.RuleFunctionDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapperImpl;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.FilePublicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.PublicationResourceIssue;
import com.epam.aidial.cfg.model.PublicationStatus;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.service.ApplicationResourceService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationPublicationResolverTest {
    private static final String PUBLICATION_PATH = "testPublication";
    private static final String FULL_PATH = "publications/" + PUBLICATION_PATH;
    private static final String APPLICATION_PREFIX = "applications/";
    private static final String FILES_PREFIX = "files/";
    private static final String APPLICATION_NAME = "testApplication";
    private static final String FILE_NAME = "testFile";
    private static final String REVIEW_FOLDER = "reviewFolder/";
    private static final String REVIEW_APPLICATION_PATH = REVIEW_FOLDER + APPLICATION_NAME;
    private static final String TARGET_FOLDER = "targetFolder/";
    private static final String TARGET_APPLICATION_PATH = TARGET_FOLDER + APPLICATION_NAME;
    private static final String SOURCE_FOLDER = "sourceFolder/";
    private static final String SOURCE_FOLDER_PATH = SOURCE_FOLDER + APPLICATION_NAME;

    @Mock
    private PublicationResourceUrlResolver publicationResourceUrlResolver;
    @Mock
    private ApplicationResourceService applicationService;
    @Mock
    private ApplicationClientMapper applicationClientMapper;
    @Mock
    private FilePublicationResolver filePublicationResolver;
    @Spy
    private PublicationClientMapperImpl publicationClientMapper;

    @InjectMocks
    private ApplicationPublicationResolver applicationPublicationResolver;

    @Test
    void resolvePublicationShouldReturnCorrectApplicationPublication() {
        // given
        var publicationResource = getPublicationResourceDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource), List.of(ResourceTypeDto.APPLICATION), List.of(ruleDto));

        var applicationResource = new ApplicationResource();
        applicationResource.setPath(REVIEW_APPLICATION_PATH);
        applicationResource.setFolderId(REVIEW_FOLDER);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING)).thenReturn(APPLICATION_PREFIX + REVIEW_APPLICATION_PATH);
        when(applicationService.getApplicationResource(REVIEW_APPLICATION_PATH)).thenReturn(applicationResource);

        // when
        var result = applicationPublicationResolver.resolvePublication(publicationDto);

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
        assertThat(resource).isInstanceOf(ApplicationPublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var applicationPublicationResource = (ApplicationPublicationResource) resource;
        assertThat(applicationPublicationResource.getApplicationResource()).isNotNull();

        var actualApplicationResource = applicationPublicationResource.getApplicationResource();
        assertThat(actualApplicationResource).isNotNull();
        assertThat(actualApplicationResource.getPath()).isEqualTo(REVIEW_APPLICATION_PATH);
        assertThat(actualApplicationResource.getFolderId()).isEqualTo(REVIEW_FOLDER);

        assertThat(result).isInstanceOf(ApplicationPublication.class);
        var applicationPublication = (ApplicationPublication) result;
        assertThat(applicationPublication.getFiles()).isEmpty();
    }

    @Test
    void resolvePublicationShouldReturnCorrectApplicationPublicationWithFiles() {
        // given
        var publicationResource = getPublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        var filePublicationResource = getFilePublicationDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource, filePublicationResource), List.of(ResourceTypeDto.FILE, ResourceTypeDto.APPLICATION), List.of(ruleDto));
        var applicationResource = new ApplicationResource();
        applicationResource.setPath(REVIEW_APPLICATION_PATH);
        applicationResource.setFolderId(REVIEW_FOLDER);
        var fileResource = getFilePublicationResource();

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING)).thenReturn(APPLICATION_PREFIX + REVIEW_APPLICATION_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING)).thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(applicationService.getApplicationResource(REVIEW_APPLICATION_PATH)).thenReturn(applicationResource);
        when(filePublicationResolver.resolveFileResourcePaths(anyList(), anyList())).thenReturn(List.of(fileResource));

        // when
        var result = applicationPublicationResolver.resolvePublication(publicationDto);

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
        assertThat(resource).isInstanceOf(ApplicationPublicationResource.class);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceAction.ADD);

        var applicationPublicationResource = (ApplicationPublicationResource) resource;
        assertThat(applicationPublicationResource.getApplicationResource()).isNotNull();

        var actualApplicationResource = applicationPublicationResource.getApplicationResource();
        assertThat(actualApplicationResource).isNotNull();
        assertThat(actualApplicationResource.getPath()).isEqualTo(REVIEW_APPLICATION_PATH);
        assertThat(actualApplicationResource.getFolderId()).isEqualTo(REVIEW_FOLDER);

        assertThat(result).isInstanceOf(ApplicationPublication.class);
        var applicationPublication = (ApplicationPublication) result;
        assertThat(applicationPublication.getFiles()).containsExactly(fileResource);
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

    @Test
    void resolvePublicationShouldReturnSingleMissingResourceWhenSomeResourceNotPresent() {
        // given
        var publicationResource = getPublicationResourceDto();
        var filePublicationResource = getFilePublicationDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource, filePublicationResource), List.of(ResourceTypeDto.FILE, ResourceTypeDto.APPLICATION), List.of(ruleDto));
        var applicationResource = new ApplicationResource();
        applicationResource.setPath(REVIEW_APPLICATION_PATH);
        applicationResource.setFolderId(REVIEW_FOLDER);
        var fileResource = getFilePublicationResource();

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING)).thenReturn(APPLICATION_PREFIX + REVIEW_APPLICATION_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource, PublicationStatusDto.PENDING)).thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(applicationService.getApplicationResource(REVIEW_APPLICATION_PATH)).thenThrow(ResourceNotFoundException.class);
        when(filePublicationResolver.resolveFileResourcePaths(anyList(), anyList())).thenReturn(List.of(fileResource));

        // when
        var result = applicationPublicationResolver.resolvePublication(publicationDto);

        assertThat(result.getResourceIssues()).hasSize(1);
        var missingResource = result.getResourceIssues().get(0);
        assertThat(missingResource.getResourceType()).isEqualTo(ResourceType.APPLICATION);
        assertThat(missingResource.getMessage()).isEqualTo("Application not found");
        assertThat(missingResource.getPath()).isEqualTo("reviewFolder/testApplication");
    }

    @Test
    void resolvePublicationShouldReturnManyMissingResourceWhenSomeResourceNotPresent() {
        // given
        var publicationResource = getPublicationResourceDto();
        var filePublicationResource1 = getFilePublicationDto();
        var filePublicationResource2 = getFilePublicationDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource, filePublicationResource1, filePublicationResource2),
                List.of(ResourceTypeDto.FILE, ResourceTypeDto.APPLICATION), List.of(ruleDto));
        var applicationResource = new ApplicationResource();
        applicationResource.setPath(REVIEW_APPLICATION_PATH);
        applicationResource.setFolderId(REVIEW_FOLDER);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING)).thenReturn(APPLICATION_PREFIX + REVIEW_APPLICATION_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource1, PublicationStatusDto.PENDING)).thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(applicationService.getApplicationResource(REVIEW_APPLICATION_PATH)).thenThrow(ResourceNotFoundException.class);
        doAnswer(invocation -> {
            List<PublicationResourceIssue> missing = invocation.getArgument(1);
            missing.add(new PublicationResourceIssue(ResourceType.FILE, "/missing/file", "File not found"));
            return List.of(REVIEW_FOLDER + FILE_NAME);
        }).when(filePublicationResolver).resolveFileResourcePaths(anyList(), anyList());

        // when
        var result = (ApplicationPublication) applicationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getResourceIssues()).hasSize(2);
        assertThat(result.getFiles()).hasSize(1);
        var missingResource1 = result.getResourceIssues().get(0);
        assertThat(missingResource1.getResourceType()).isEqualTo(ResourceType.APPLICATION);
        assertThat(missingResource1.getMessage()).isEqualTo("Application not found");
        assertThat(missingResource1.getPath()).isEqualTo("reviewFolder/testApplication");
        var missingResource2 = result.getResourceIssues().get(1);
        assertThat(missingResource2.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(missingResource2.getMessage()).isEqualTo("File not found");
        assertThat(missingResource2.getPath()).isEqualTo("/missing/file");
    }

    @Test
    void resolvePublicationShouldReturnResourceIssueWhenSomeResourceIsPublished() {
        // given
        var publicationResource = getPublicationResourceDto();
        var filePublicationResource1 = getFilePublicationDto();
        var filePublicationResource2 = getFilePublicationDto();
        var ruleDto = getRuleDto();
        var publicationDto = getPublicationDto(List.of(publicationResource, filePublicationResource1, filePublicationResource2),
                List.of(ResourceTypeDto.FILE, ResourceTypeDto.APPLICATION), List.of(ruleDto));
        var applicationResource = new ApplicationResource();
        applicationResource.setPath(REVIEW_APPLICATION_PATH);
        applicationResource.setFolderId(REVIEW_FOLDER);

        when(publicationResourceUrlResolver.resolveUrl(publicationResource, PublicationStatusDto.PENDING)).thenReturn(APPLICATION_PREFIX + REVIEW_APPLICATION_PATH);
        when(publicationResourceUrlResolver.resolveUrl(filePublicationResource1, PublicationStatusDto.PENDING)).thenReturn(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME);
        when(applicationService.applicationResourceExists(anyString())).thenReturn(true);
        doAnswer(invocation -> {
            List<PublicationResourceIssue> missing = invocation.getArgument(1);
            missing.add(new PublicationResourceIssue(ResourceType.FILE, "/missing/file", "File not found"));
            return List.of(REVIEW_FOLDER + FILE_NAME);
        }).when(filePublicationResolver).resolveFileResourcePaths(anyList(), anyList());

        // when
        var result = (ApplicationPublication) applicationPublicationResolver.resolvePublication(publicationDto);

        // then
        assertThat(result.getResourceIssues()).hasSize(2);
        assertThat(result.getFiles()).hasSize(1);
        var missingResource1 = result.getResourceIssues().get(0);
        assertThat(missingResource1.getResourceType()).isEqualTo(ResourceType.APPLICATION);
        assertThat(missingResource1.getMessage()).isEqualTo("Target application already exists");
        assertThat(missingResource1.getPath()).isEqualTo("applications/targetFolder/testApplication");
        var missingResource2 = result.getResourceIssues().get(1);
        assertThat(missingResource2.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(missingResource2.getMessage()).isEqualTo("File not found");
        assertThat(missingResource2.getPath()).isEqualTo("/missing/file");
    }

    @Test
    void updatePublicationResourcesShouldReturnCorrectApplicationPublicationWithFiles() {
        // given
        var applicationPublication = createApplicationPublication();
        var fileResource = getFilePublicationResource();

        when(applicationClientMapper.toCreateApplicationResource(any())).thenReturn(new CreateApplicationResource());
        MockMultipartFile publicationFile = new MockMultipartFile("publication", "publication.json", MimeTypeUtils.APPLICATION_JSON_VALUE,
                "dtoJson".getBytes(StandardCharsets.UTF_8));
        when(filePublicationResolver.updateFileResources(any(), any(), anyString())).thenReturn(List.of(fileResource));

        // when
        var result = applicationPublicationResolver.updatePublicationResources(applicationPublication, List.of(publicationFile));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUrl()).isEqualTo("publications/reviewFolder/testApplication");
        assertThat(result.getName()).isEqualTo("Test Publication");
        assertThat(result.getAuthor()).isEqualTo("Author Name");
        assertThat(result.getStatus()).isEqualTo(PublicationStatusDto.PENDING);
        assertThat(result.getTargetFolder()).isEqualTo(TARGET_FOLDER);

        assertThat(result.getRules()).hasSize(1);
        var rule = result.getRules().get(0);
        assertThat(rule.getSource()).isEqualTo("role");
        assertThat(rule.getFunction()).isEqualTo(RuleFunctionDto.EQUAL);
        assertThat(rule.getTargets()).containsExactly("admin");

        assertThat(result.getResources()).hasSize(2);
        var resource = result.getResources().get(0);
        assertThat(resource.getAction()).isEqualTo(PublicationResourceActionDto.ADD);
        assertThat(resource.getSourceUrl()).isEqualTo("applications/sourceFolder/testApplication");
        assertThat(resource.getTargetUrl()).isEqualTo("applications/targetFolder/testApplication");
        assertThat(resource.getReviewUrl()).isEqualTo("applications/reviewFolder/testApplication");
        var resource1 = result.getResources().get(1);
        assertThat(resource1.getAction()).isEqualTo(PublicationResourceActionDto.ADD_IF_ABSENT);
        assertThat(resource1.getSourceUrl()).isEqualTo("files/sourceFolder/testFile");
        assertThat(resource1.getTargetUrl()).isEqualTo("files/targetFolder/testFile");
        assertThat(resource1.getReviewUrl()).isEqualTo("files/reviewFolder/testFile");
    }

    private PublicationResourceDto getPublicationResourceDto() {
        var publicationResource = new PublicationResourceDto();
        publicationResource.setAction(PublicationResourceActionDto.ADD);
        publicationResource.setTargetUrl(APPLICATION_PREFIX + TARGET_APPLICATION_PATH);
        publicationResource.setReviewUrl(APPLICATION_PREFIX + REVIEW_APPLICATION_PATH);
        publicationResource.setSourceUrl(APPLICATION_PREFIX + SOURCE_FOLDER_PATH);
        return publicationResource;
    }

    private RuleDto getRuleDto() {
        var ruleDto = new RuleDto();
        ruleDto.setSource("role");
        ruleDto.setFunction(RuleFunctionDto.EQUAL);
        ruleDto.setTargets(List.of("admin"));
        return ruleDto;
    }

    private PublicationDto getPublicationDto(List<PublicationResourceDto> publicationResources, List<ResourceTypeDto> types, List<RuleDto> rules) {
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

    private ApplicationPublication createApplicationPublication() {
        var applicationResource = new ApplicationResource();
        applicationResource.setPath(REVIEW_APPLICATION_PATH);
        applicationResource.setFolderId(REVIEW_FOLDER);

        var applicationPublicationResource = ApplicationPublicationResource.builder()
                .action(PublicationResourceAction.ADD)
                .targetUrl(APPLICATION_PREFIX + TARGET_FOLDER + APPLICATION_NAME)
                .reviewUrl(APPLICATION_PREFIX + REVIEW_FOLDER + APPLICATION_NAME)
                .sourceUrl(APPLICATION_PREFIX + SOURCE_FOLDER + APPLICATION_NAME)
                .applicationResource(applicationResource)
                .build();

        var rule = new Rule();
        rule.setSource("role");
        rule.setFunction(RuleFunction.EQUAL);
        rule.setTargets(List.of("admin"));

        return ApplicationPublication.builder()
                .path(REVIEW_FOLDER + APPLICATION_NAME)
                .createdAt(100)
                .requestName("Test Publication")
                .displayAuthor("Display Author Name")
                .author("Author Name")
                .folderId(TARGET_FOLDER)
                .status(PublicationStatus.PENDING)
                .rules(List.of(rule))
                .resources(List.of(applicationPublicationResource))
                .files(List.of(getFilePublicationResource()))
                .build();
    }

    private FilePublicationResource getFilePublicationResource() {
        return FilePublicationResource.builder()
                .file(FileNodeInfo.builder()
                        .path("test")
                        .build())
                .action(PublicationResourceAction.ADD_IF_ABSENT)
                .targetUrl(FILES_PREFIX + TARGET_FOLDER + FILE_NAME)
                .reviewUrl(FILES_PREFIX + REVIEW_FOLDER + FILE_NAME)
                .sourceUrl(FILES_PREFIX + SOURCE_FOLDER + FILE_NAME)
                .build();
    }

}