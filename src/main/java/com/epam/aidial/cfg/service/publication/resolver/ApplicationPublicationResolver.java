package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.ResourceAlreadyExistsException;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationResourceIssue;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.ApplicationResourceService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import com.epam.aidial.cfg.utils.PathUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Component
@LogExecution
public class ApplicationPublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final ApplicationResourceService applicationService;
    private final ApplicationClientMapper applicationClientMapper;
    private final FilePublicationResolver filePublicationResolver;

    protected ApplicationPublicationResolver(PublicationResourceUrlResolver resolver,
                                             PublicationClientMapper mapper,
                                             ApplicationResourceService applicationService,
                                             ApplicationClientMapper applicationClientMapper,
                                             FilePublicationResolver filePublicationResolver) {
        super(resolver);
        this.mapper = mapper;
        this.applicationService = applicationService;
        this.applicationClientMapper = applicationClientMapper;
        this.filePublicationResolver = filePublicationResolver;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);
        var status = publicationDto.getStatus();
        var resourceInfoList = publicationDto.getResources().stream()
                .map(resourceInfo(status))
                .toList();
        List<PublicationResourceIssue> resourceIssues = new ArrayList<>();
        var applicationResources = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(ApplicationClientMapper.APPLICATIONS_PREFIX))
                .map(app -> resolveResourceAndCollectIssues(
                        () -> getApplicationPublication(app, status),
                        resourceIssues,
                        new PublicationResourceIssue(ResourceType.APPLICATION, extractApplicationPath(app),
                                "Application not found"),
                        new PublicationResourceIssue(ResourceType.APPLICATION, app.resource().getTargetUrl(),
                                "Target application already exists")))
                .flatMap(Optional::stream)
                .toList();

        var files = filePublicationResolver.resolveFileResourcePaths(resourceInfoList, resourceIssues);

        return mapper.toApplicationPublication(publicationDto, applicationResources, files, resourceIssues);
    }

    @Override
    public void updatePublicationResources(Publication publication) {
        var applicationPublication = (ApplicationPublication) publication;
        var applications = applicationPublication.getResources();
        applications.stream()
                .map(ApplicationPublicationResource::getApplicationResource)
                .map(applicationClientMapper::toCreateApplicationResource)
                .forEach(application -> applicationService.putApplicationResource(application, true, null));
    }

    @Override
    public PublicationDto updatePublicationResourceTargets(Publication publication) {
        var applicationPublication = (ApplicationPublication) publication;
        var folderId = publication.getFolderId();

        var updatedApplicationResources = applicationPublication.getResources().stream()
                .map(application -> recalculateTargetUrl(application, folderId))
                .toList();
        var updatedFileResources = Optional.ofNullable(applicationPublication.getFiles())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(file -> filePublicationResolver.recalculateTargetUrl(file, folderId))
                .toList();

        var updatedResources = Stream.concat(
                        updatedApplicationResources.stream(),
                        updatedFileResources.stream())
                .toList();
        return mapper.toPublicationDto(publication, updatedResources);
    }

    private ApplicationPublicationResource recalculateTargetUrl(ApplicationPublicationResource resource, String folderId) {
        var folder = PathUtils.ensureTrailingSlash(folderId);
        var applicationResource = resource.getApplicationResource();
        var newTargetPath = PathUtils.buildEncodedPath(ApplicationClientMapper.APPLICATIONS_PREFIX + folder,
                applicationResource.getName(), applicationResource.getVersion());
        resource.setTargetUrl(newTargetPath);
        return resource;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.APPLICATION;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.APPLICATION, ResourceTypeDto.FILE);
    }

    public void attachUploadedFiles(Publication publication, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        var applicationPublication = (ApplicationPublication) publication;
        applicationPublication.setFiles(
                filePublicationResolver.merge(
                        applicationPublication.getFiles(),
                        filePublicationResolver.uploadNewFileResources(files, publication.getFolderId())));
    }

    private ApplicationPublicationResource getApplicationPublication(ResourceInfo resourceInfo, PublicationStatusDto status) {
        var resource = resourceInfo.resource();
        validateTargetNotPublished(resourceInfo, status);
        var applicationPath = extractApplicationPath(resourceInfo);
        var applicationResource = applicationService.getApplicationResource(applicationPath);
        return mapper.toApplicationPublicationResource(resource, applicationResource);
    }

    public void validateTargetNotPublished(ResourceInfo resourceInfo, PublicationStatusDto status) {
        var insideResource = resourceInfo.resource();
        if (status == PublicationStatusDto.PENDING && insideResource.getAction() != PublicationResourceActionDto.DELETE) {
            var targetUrl = extractTargetPath(resourceInfo, ApplicationClientMapper.APPLICATIONS_PREFIX);
            validateNotPublishedAtPath(targetUrl);
        }
    }

    public void validateNotPublishedAtPath(String targetUrl) {
        if (applicationService.applicationResourceExists(targetUrl)) {
            throw new ResourceAlreadyExistsException("Target application already exists");
        }
    }

    private String extractApplicationPath(ResourceInfo resourceInfo) {
        return extractPath(resourceInfo, ApplicationClientMapper.APPLICATIONS_PREFIX);
    }
}