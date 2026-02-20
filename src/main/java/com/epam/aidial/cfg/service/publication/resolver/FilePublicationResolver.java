package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.PublicationFileUploadException;
import com.epam.aidial.cfg.exception.ResourceAlreadyExistsException;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ConversationPublication;
import com.epam.aidial.cfg.model.FilePublication;
import com.epam.aidial.cfg.model.FilePublicationResource;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.PublicationResourceIssue;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.ToolSetPublication;
import com.epam.aidial.cfg.service.BucketService;
import com.epam.aidial.cfg.service.FileService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import com.epam.aidial.cfg.utils.PathUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Component
@LogExecution
public class FilePublicationResolver extends PublicationResolver {

    public static final String PUBLICATIONS_UPDATES_FOLDER = "publications_updates/";

    private final PublicationClientMapper mapper;
    private final FileClientMapper fileClientMapper;
    private final FileService fileService;
    private final BucketService bucketService;

    protected FilePublicationResolver(PublicationResourceUrlResolver resolver,
                                      PublicationClientMapper mapper,
                                      FileClientMapper fileClientMapper,
                                      FileService fileService, BucketService bucketService) {
        super(resolver);
        this.mapper = mapper;
        this.fileClientMapper = fileClientMapper;
        this.fileService = fileService;
        this.bucketService = bucketService;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var status = publicationDto.getStatus();
        List<PublicationResourceIssue> resourceIssues = new ArrayList<>();
        var fileResources = publicationDto.getResources().stream()
                .map(resourceInfo(status))
                .map(file -> resolveResourceAndCollectIssues(
                        () -> getFilePublication(file, status),
                        resourceIssues,
                        new PublicationResourceIssue(ResourceType.FILE, extractFilePath(file.resource(), status),
                                "File not found"),
                        new PublicationResourceIssue(ResourceType.FILE, file.resource().getTargetUrl(),
                                "Target file already exists")))
                .flatMap(Optional::stream)
                .toList();
        return mapper.toFilePublication(publicationDto, fileResources, resourceIssues);
    }

    @Override
    public PublicationDto updatePublicationResources(Publication publication, List<MultipartFile> files) {
        var existingFileResources = getFilePublicationResources(publication);
        var updatedListFiles = updateFileResources(existingFileResources, files, publication.getFolderId());
        return mapper.toPublicationDto(publication, updatedListFiles);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.FILE;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.FILE);
    }

    private FilePublicationResource getFilePublication(ResourceInfo resourceInfo, PublicationStatusDto status) {
        var filePath = extractFilePath(resourceInfo.resource(), status);
        validateTargetNotPublished(resourceInfo, status);
        var request = ResourceMetadataRequest.builder().path(filePath).build();
        var filesNode = fileService.getAll(request);

        if (filesNode.getNodeType() != NodeType.ITEM) {
            throw new IllegalStateException("Incorrect node type: %s. Resource: %s."
                    .formatted(filesNode.getNodeType(), resourceInfo));
        }

        return mapper.toFilePublicationResource(resourceInfo.resource(), filesNode);
    }

    private String extractFilePath(PublicationResourceDto publicationResource, PublicationStatusDto status) {
        var path = resolver.resolveUrl(publicationResource, status);
        return fileClientMapper.parsePath(path).getPath();
    }

    protected List<FilePublicationResource> resolveFileResourcePaths(List<ResourceInfo> resourceInfoList,
                                                                     List<PublicationResourceIssue> resourceIssues) {
        return resourceInfoList.stream()
                .filter(resourceUrlStartsWith(FileClientMapper.FILES_PREFIX))
                .map(resource -> resolveResourceAndCollectIssues(
                        () -> getFilePublication(resource, resource.status()),
                        resourceIssues,
                        new PublicationResourceIssue(ResourceType.FILE, extractFilePath(resource.resource(), resource.status()),
                                "File not found"),
                        new PublicationResourceIssue(ResourceType.FILE, resource.resource().getTargetUrl(),
                                "Target file already exists")))
                .flatMap(Optional::stream)
                .toList();
    }

    public void validateTargetNotPublished(ResourceInfo resourceInfo, PublicationStatusDto status) {
        var insideResource = resourceInfo.resource();
        if (status == PublicationStatusDto.PENDING && insideResource.getAction() != PublicationResourceActionDto.DELETE) {
            var targetUrl = extractTargetPath(resourceInfo, FileClientMapper.FILES_PREFIX);
            validateNotPublishedAtPath(targetUrl);
        }
    }

    public void validateNotPublishedAtPath(String targetUrl) {
        if (fileService.fileExists(targetUrl)) {
            throw new ResourceAlreadyExistsException("Target file already exists");
        }
    }

    protected List<FilePublicationResource> updateFileResources(List<FilePublicationResource> existingFileResources,
                                                                List<MultipartFile> files,
                                                                String folderId) {
        if (CollectionUtils.isEmpty(files)) {
            return existingFileResources;
        }

        var sourceFolder = PathUtils.ensureTrailingSlash(bucketService.getBucket().getBucket());
        var updatesFolderPath = sourceFolder + PUBLICATIONS_UPDATES_FOLDER;
        var uploadedSourceFiles = upload(files, updatesFolderPath);
        validateUploadResult(uploadedSourceFiles);

        var newFileResources = uploadedSourceFiles.getImportResults().stream()
                .map(importResult -> getPublicationResource(importResult, folderId, updatesFolderPath))
                .toList();

        return Stream.concat(existingFileResources.stream(), newFileResources.stream()).toList();
    }

    List<FilePublicationResource> getFilePublicationResources(Publication publication) {
        List<FilePublicationResource> existingFileResources;
        if (publication instanceof ApplicationPublication applicationPublication) {
            existingFileResources = applicationPublication.getFiles();
        } else if (publication instanceof ToolSetPublication toolSetPublication) {
            existingFileResources = toolSetPublication.getFiles();
        } else if (publication instanceof ConversationPublication conversationPublication) {
            existingFileResources = conversationPublication.getFiles();
        } else if (publication instanceof FilePublication file) {
            existingFileResources = file.getResources();
        } else {
            existingFileResources = List.of();
        }
        return existingFileResources;
    }

    private ImportResourcesFileResult upload(List<MultipartFile> files, String path) {
        var importResources = ImportResources.builder().path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();
        return fileService.uploadFile(files, importResources);
    }

    private void validateUploadResult(ImportResourcesFileResult result) {
        if (result == null || result.hasError()) {
            throw new PublicationFileUploadException(
                    "Publication files upload failed: " + (result != null ? result.getError() : "null result")
            );
        }

        var errors = result.getImportResults().stream()
                .filter(importResourcesResult -> importResourcesResult.getError() != null)
                .map(importResourcesResult -> String.format(
                        "%s -> %s: %s",
                        importResourcesResult.getSourcePath(),
                        importResourcesResult.getTargetPath(),
                        importResourcesResult.getError()))
                .toList();

        if (!errors.isEmpty()) {
            throw new PublicationFileUploadException("Publication files upload failed: " + String.join("; ", errors));
        }
    }

    private FilePublicationResource getPublicationResource(
            ImportResourcesResult importResult,
            String folderId,
            String sourceFolder
    ) {
        var fileName = PathUtils.parsePath(importResult.getTargetPath()).getName();
        var targetPath = FileClientMapper.FILES_PREFIX + PathUtils.ensureTrailingSlash(folderId) + fileName;
        var sourcePath = FileClientMapper.FILES_PREFIX + sourceFolder + fileName;

        return FilePublicationResource.builder()
                .action(PublicationResourceAction.ADD_IF_ABSENT)
                .targetUrl(targetPath)
                .sourceUrl(sourcePath)
                .build();
    }
}