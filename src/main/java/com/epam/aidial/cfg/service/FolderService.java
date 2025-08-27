package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.CoreMetadataUtils;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.FolderAlreadyExistsException;
import com.epam.aidial.cfg.exception.FolderNotFoundException;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveFolderRequest;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.PublicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.publication.PublicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.client.mapper.PublicationClientMapper.PUBLICATIONS_PREFIX;

@Service
@LogExecution
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final Map<ResourceType, ResourceService> resourceServicesByResourceType;
    private final PublicationService publicationService;

    public FolderInfo getFolders(ResourceMetadataRequest request) {
        List<FolderInfo> folderInfos = resourceServicesByResourceType.values().stream()
                .map(resourceService -> resourceService.getFolders(request))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return folderInfos.isEmpty() ? null : merge(folderInfos);
    }

    public Map<String, List<Rule>> getRules(String path) {
        return publicationService.getRules(path);
    }

    public void updatesRules(UpdateRulesRequest request) {
        CreatePublication createPublication = CreatePublication.builder()
                .targetFolder(request.getTargetFolder())
                .rules(request.getRules())
                .build();
        String publication = publicationService.createPublication(createPublication);
        approvePublication(publication);
    }

    public void unpublishFolder(String path) {
        Set<String> targetUrls = resourceServicesByResourceType.values().stream()
                .map(resourceService -> resourceService.getResourceUrls(path))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        List<PublicationResource> resources = createResourcesForDeleting(targetUrls);
        CreatePublication createPublication = CreatePublication.builder()
                .targetFolder(path)
                .resources(resources)
                .build();
        String publication = publicationService.createPublication(createPublication);
        approvePublication(publication);
    }

    public void moveFolder(MoveFolderRequest moveFolderRequest) {
        String oldPath = moveFolderRequest.getOldPath();
        String newPath = moveFolderRequest.getNewPath();
        List<ResourceType> resourceTypes = Optional.ofNullable(moveFolderRequest.getResourceTypes())
                .orElseGet(() -> Arrays.stream(ResourceType.values()).toList());

        checkFolderExists(oldPath, resourceTypes);
        checkFolderDoesNotExist(newPath);
        copyFolderRules(oldPath, newPath);
        moveResources(oldPath, newPath, resourceTypes);
    }

    private List<PublicationResource> createResourcesForDeleting(Set<String> targetUrls) {
        return targetUrls.stream()
                .map(url -> PublicationResource.builder()
                        .action(PublicationResourceAction.DELETE)
                        .targetUrl(url)
                        .build())
                .collect(Collectors.toList());
    }

    private void approvePublication(String publication) {
        String path = CoreMetadataUtils.removeMetadataPrefix(publication, PUBLICATIONS_PREFIX);
        publicationService.approvePublication(path);
    }

    private FolderInfo merge(List<FolderInfo> folderInfos) {
        validateFolderInfoConsistency(folderInfos);
        List<FolderInfo> items = mergeAndSortItems(filterNullItems(folderInfos));
        return FolderInfo.builder()
                .name(folderInfos.get(0).getName())
                .parentPath(folderInfos.get(0).getParentPath())
                .bucket(folderInfos.get(0).getBucket())
                .path(folderInfos.get(0).getPath())
                .items(items)
                .build();
    }

    private void validateFolderInfoConsistency(List<FolderInfo> folderInfos) {
        String name = folderInfos.get(0).getName();
        String parentPath = folderInfos.get(0).getParentPath();
        String bucket = folderInfos.get(0).getBucket();
        String path = folderInfos.get(0).getPath();

        for (FolderInfo folderInfo : folderInfos) {
            validateConsistency("Name", name, folderInfo.getName());
            validateConsistency("ParentPath", parentPath, folderInfo.getParentPath());
            validateConsistency("Bucket", bucket, folderInfo.getBucket());
            validateConsistency("Path", path, folderInfo.getPath());
        }
    }

    private void validateConsistency(String fieldName, String expected, String actual) {
        if (!Objects.equals(expected, actual)) {
            log.error("{} inconsistency detected. Expected '{}', but found '{}'.", fieldName, expected, actual);
            throw new IllegalArgumentException(String.format(
                    "%s inconsistency detected. Expected '%s', but found '%s'.",
                    fieldName, expected, actual));
        }
    }

    private List<FolderInfo> filterNullItems(List<FolderInfo> folders) {
        return folders
                .stream()
                .map(FolderInfo::getItems)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }

    @SafeVarargs
    private List<FolderInfo> mergeAndSortItems(List<FolderInfo>... itemLists) {
        return Arrays.stream(itemLists)
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        FolderInfo::getPath,
                        folder -> folder,
                        (existing, incoming) -> {
                            List<FolderInfo> mergedItems = mergeAndSortItems(filterNullItems(existing.getItems()), filterNullItems(incoming.getItems()));
                            existing.setItems(mergedItems);
                            return existing;
                        }
                )).values()
                .stream()
                .sorted(Comparator.comparing(FolderInfo::getPath))
                .toList();
    }

    private void checkFolderExists(String path, List<ResourceType> resourceTypes) {
        ResourceMetadataRequest resourceMetadataRequest = ResourceMetadataRequest.builder()
                .path(path)
                .build();
        for (var resourceType : resourceTypes) {
            ResourceService resourceService = resolveResourceService(resourceType);
            if (resourceService.getFolders(resourceMetadataRequest) == null) {
                throw new FolderNotFoundException("Folder: " + path + " does not exist in " + resourceService.getResourceType() + " resources");
            }
        }
    }

    private void checkFolderDoesNotExist(String path) {
        ResourceMetadataRequest resourceMetadataRequest = ResourceMetadataRequest.builder()
                .path(path)
                .build();
        for (var resourceService : resourceServicesByResourceType.values()) {
            if (resourceService.getFolders(resourceMetadataRequest) != null) {
                throw new FolderAlreadyExistsException("Folder: " + path + " already exists in " + resourceService.getResourceType() + " resources");
            }
        }
    }

    private void copyFolderRules(String oldPath, String newPath) {
        List<Rule> rules = getRules(oldPath).values().stream()
                .flatMap(Collection::stream)
                .toList();
        UpdateRulesRequest updateRulesRequest = UpdateRulesRequest.builder()
                .targetFolder(newPath)
                .rules(rules)
                .build();
        updatesRules(updateRulesRequest);
    }

    private void moveResources(String oldPath, String newPath, List<ResourceType> resourceTypes) {
        List<String> movedResources = new ArrayList<>();

        for (var resourceType : resourceTypes) {
            ResourceService resourceService = resolveResourceService(resourceType);
            Set<String> resourceUrls = resourceService.getResourceUrls(oldPath);

            for (var resourceUrl : resourceUrls) {
                try {
                    MoveResource moveResource = MoveResource.builder()
                            .sourceUrl(resourceUrl)
                            .destinationUrl(resourceUrl.replace(oldPath, newPath))
                            .build();
                    resourceService.move(moveResource);
                    movedResources.add(resourceUrl);
                } catch (Exception exception) {
                    log.warn("Unable to move resource: {}, moved resources: {}", resourceUrl, movedResources, exception);
                    throw exception;
                }
            }
        }
    }

    private ResourceService resolveResourceService(ResourceType resourceType) {
        return Optional.ofNullable(resourceServicesByResourceType.get(resourceType))
                .orElseThrow(() -> new IllegalStateException("Unable to find resource service. Resource type: " + resourceType));
    }

}
