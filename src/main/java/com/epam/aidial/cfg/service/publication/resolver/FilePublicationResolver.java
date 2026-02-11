package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.PublicationFileUploadException;
import com.epam.aidial.cfg.model.FilePublicationResource;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationMissingResource;
import com.epam.aidial.cfg.model.PublicationResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.FileService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import com.epam.aidial.cfg.utils.PathUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@LogExecution
public class FilePublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final FileClientMapper fileClientMapper;
    private final FileService fileService;

    protected FilePublicationResolver(PublicationResourceUrlResolver resolver,
                                      PublicationClientMapper mapper,
                                      FileClientMapper fileClientMapper,
                                      FileService fileService) {
        super(resolver);
        this.mapper = mapper;
        this.fileClientMapper = fileClientMapper;
        this.fileService = fileService;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        List<PublicationMissingResource> missingResources = new ArrayList<>();
        var fileResources = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .map(file -> resolveResourceAndCollectMissing(
                        () -> getFilePublication(file.resource(), file.status()),
                        ResourceType.FILE,
                        extractFilePath(file.resource(), file.status()),
                        missingResources,
                        "File not found"))
                .flatMap(Optional::stream)
                .toList();
        return mapper.toFilePublication(publicationDto, fileResources, missingResources);
    }

    @Override
    public PublicationDto resolveUpdatePublication(Publication publication, List<MultipartFile> files) {
        var listFiles = resolveUpdateFileResource(publication, files);
        return mapper.toPublicationDto(publication, listFiles);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.FILE;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.FILE);
    }

    private FilePublicationResource getFilePublication(PublicationResourceDto resource, PublicationStatusDto status) {
        var filePath = extractFilePath(resource, status);
        var request = ResourceMetadataRequest.builder().path(filePath).build();
        var filesNode = fileService.getAll(request);

        if (filesNode.getNodeType() != NodeType.ITEM) {
            throw new IllegalStateException("Incorrect node type: %s. Resource: %s."
                    .formatted(filesNode.getNodeType(), resource));
        }

        return mapper.toFilePublicationResource(resource, filesNode);
    }

    private String extractFilePath(PublicationResourceDto publicationResource, PublicationStatusDto status) {
        var path = resolver.resolveUrl(publicationResource, status);
        return fileClientMapper.parsePath(path).getPath();
    }

    protected List<String> resolveFileResourcePaths(List<ResourceInfo> resourceInfoList, List<PublicationMissingResource> missingResources) {
        return resourceInfoList.stream()
                .filter(resourceUrlStartsWith(FileClientMapper.FILES_PREFIX))
                .map(resource -> resolveResourceAndCollectMissing(
                        () -> {
                            getFilePublication(resource.resource(), resource.status());
                            return extractFilePath(resource);
                        },
                        ResourceType.FILE,
                        extractFilePath(resource),
                        missingResources,
                        "File not found"))
                .flatMap(Optional::stream)
                .toList();
    }

    protected List<PublicationResource> resolveUpdateFileResource(Publication publication, List<MultipartFile> files) {
        var existingFileResources = publication.getResources().stream()
                .filter(publicationResourceUrlStartsWith(FileClientMapper.FILES_PREFIX)).toList();

        var resultList = new ArrayList<PublicationResource>(existingFileResources);

        if (files.isEmpty()) {
            return resultList;
        }

        var reviewFolder = publication.getReviewFolderId();
        var sourceFolder = publication.getReviewFolderId() + "source/";

        var uploadedReviewFiles = upload(files, reviewFolder);
        var uploadedSourceFiles = upload(files, sourceFolder);

        validateUploadResult(uploadedReviewFiles);
        validateUploadResult(uploadedSourceFiles);

        var newResources = uploadedReviewFiles.getImportResults().stream()
                .map(importResult -> getPublicationResource(importResult, publication, sourceFolder))
                .toList();

        resultList.addAll(newResources);

        return resultList;
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

    private PublicationResource getPublicationResource(
            ImportResourcesResult importResult,
            Publication publication,
            String reviewSourceFolder
    ) {
        var fileName = PathUtils.parsePath(importResult.getTargetPath()).getName();

        return mapper.toPublicationResource(
                importResult,
                publication.getFolderId() + fileName,
                reviewSourceFolder + fileName
        );
    }
}