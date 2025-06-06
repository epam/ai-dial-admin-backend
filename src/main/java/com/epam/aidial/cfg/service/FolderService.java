package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.CoreMetadataUtils;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.validation.annotation.MetadataPath;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.PublicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.publication.PublicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.client.mapper.PublicationClientMapper.PUBLICATIONS_PREFIX;

@Service
@LogExecution
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final List<ResourceService> resourceServices;
    private final PublicationService publicationService;

    public FolderInfo getFolders(ResourceMetadataRequest request) {
        List<FolderInfo> folderInfos = resourceServices.stream()
                .map(resourceService -> resourceService.getFolders(request))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return folderInfos.isEmpty() ? null : merge(folderInfos);
    }

    public Map<String, List<Rule>> getRules(@MetadataPath String path) {
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

    public void unpublishFolder(@MetadataPath String path) {
        Set<String> targetUrls = resourceServices.stream()
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

}
