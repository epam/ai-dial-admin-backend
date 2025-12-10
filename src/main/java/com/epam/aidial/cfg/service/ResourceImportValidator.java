package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.model.ImportResources;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Validated
@Component
@LogExecution
public class ResourceImportValidator {
    private static final String APPLICATION_RESOURCE = "Application";
    private static final String TOOLSET_RESOURCE = "ToolSet";
    private static final String DUPLICATES_WITHIN_FILES_FLAT_IMPORT = "\n    - File '%s' has duplicate %s: name '%s', version '%s'";
    private static final String DUPLICATES_WITHIN_FILES_NON_FLAT_IMPORT = DUPLICATES_WITHIN_FILES_FLAT_IMPORT + ", folder '%s'";
    private static final String DUPLICATES_ACROSS_FILES_FLAT_IMPORT = "\n    - %s with name '%s', version '%s' found in multiple files: %s";
    private static final String DUPLICATES_ACROSS_FILES_NON_FLAT_IMPORT = "\n    - %s with name '%s', version '%s', folder '%s found in multiple files: %s";

    private record ResourceNameAndVersionAndPath(String name, String version, String folder) {
    }

    public void validateApplicationImport(ImportResources importApplications, @Valid ApplicationsEximDto applicationsEximDto) {
        var isFlatImport = importApplications.isFlatImport();
        var resources = applicationsEximDto.getApplications().stream()
                .map(app -> new ResourceNameAndVersionAndPath(app.getName(), app.getVersion(), isFlatImport ? null : app.getFolderId()))
                .toList();
        validateResourceUniqueness(resources, isFlatImport, APPLICATION_RESOURCE);
    }

    public void validateToolSetImport(ImportResources importToolsets, @Valid ToolSetsEximDto toolSetsEximDto) {
        var isFlatImport = importToolsets.isFlatImport();
        var resources = toolSetsEximDto.getToolSets().stream()
                .map(t -> new ResourceNameAndVersionAndPath(t.getName(), t.getVersion(), isFlatImport ? null : t.getFolderId()))
                .toList();
        validateResourceUniqueness(resources, isFlatImport, TOOLSET_RESOURCE);
    }

    private void validateResourceUniqueness(List<ResourceNameAndVersionAndPath> resources,
                                            boolean isFlatImport,
                                            String resourceType) {

        var duplicatedResourceNames = getDuplicateResourceNamesAndPath(resources);

        if (duplicatedResourceNames.isEmpty()) {
            return;
        }
        var errorMessage = new StringBuilder(String.format("%s uniqueness violation. Conflicts found:", resourceType));
        duplicatedResourceNames.forEach(resource -> {
            if (isFlatImport) {
                errorMessage.append("\n - Duplicated %s name '%s' and version '%s'"
                        .formatted(resourceType.toLowerCase(), resource.name, resource.version));
            } else {
                errorMessage.append("\n - Duplicated %s name '%s' and version '%s' and folder '%s'"
                        .formatted(resourceType.toLowerCase(), resource.name, resource.version, resource.folder));
            }
        });
        throw new IllegalArgumentException(errorMessage.toString());
    }

    private Set<ResourceNameAndVersionAndPath> getDuplicateResourceNamesAndPath(List<ResourceNameAndVersionAndPath> resources) {
        var counts = resources.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return counts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void checkApplicationConflicts(ImportResources importApplications, Map<String, ApplicationsEximDto> fileNameToApplicationsEximDtos) {
        var isFlatImport = importApplications.isFlatImport();
        var fileNameToListResources = toMapOfResourceNameAndVersionAndPath(
                fileNameToApplicationsEximDtos,
                ApplicationsEximDto::getApplications,
                app -> new ResourceNameAndVersionAndPath(app.getName(), app.getVersion(), isFlatImport ? null : app.getFolderId()));

        checkResourcesExistence(fileNameToListResources, APPLICATION_RESOURCE);
        checkResourcesConflicts(isFlatImport, fileNameToListResources, APPLICATION_RESOURCE);
    }

    public void checkToolSetConflicts(ImportResources importToolSets, HashMap<String, ToolSetsEximDto> fileNameToToolSetsEximDtos) {
        var isFlatImport = importToolSets.isFlatImport();
        var fileNameToListResources = toMapOfResourceNameAndVersionAndPath(
                fileNameToToolSetsEximDtos,
                ToolSetsEximDto::getToolSets,
                app -> new ResourceNameAndVersionAndPath(app.getName(), app.getVersion(), isFlatImport ? null : app.getFolderId()));
        checkResourcesExistence(fileNameToListResources, TOOLSET_RESOURCE);
        checkResourcesConflicts(isFlatImport, fileNameToListResources, TOOLSET_RESOURCE);
    }

    private void checkResourcesConflicts(boolean isFlatImport,
                                         Map<String, List<ResourceNameAndVersionAndPath>> fileNameToListResources,
                                         String resourceType) {

        var duplicatesWithinFiles = findSameResourcesWithinSameFiles(fileNameToListResources);
        var duplicatesAcrossFiles = findSameResourcesWithinDifferentFiles(fileNameToListResources);

        var hasConflicts = !duplicatesWithinFiles.isEmpty() || !duplicatesAcrossFiles.isEmpty();

        if (hasConflicts) {
            var errorMessage = new StringBuilder(String.format("%s uniqueness violation. Conflicts found:", resourceType));
            if (!duplicatesWithinFiles.isEmpty()) {
                errorMessage.append(String.format("\n  %ss duplicated within the same file:", resourceType));
                duplicatesWithinFiles.forEach((filename, resources) -> resources.forEach(resource -> {
                    if (isFlatImport) {
                        errorMessage.append(String.format(DUPLICATES_WITHIN_FILES_FLAT_IMPORT,
                                filename, resourceType.toLowerCase(), resource.name, resource.version()));
                    } else {
                        errorMessage.append(String.format(DUPLICATES_WITHIN_FILES_NON_FLAT_IMPORT,
                                filename, resourceType.toLowerCase(), resource.name, resource.version(), resource.folder));
                    }
                }));
            }

            if (!duplicatesAcrossFiles.isEmpty()) {
                errorMessage.append(String.format("\n  %ss shared across different files:", resourceType));
                duplicatesAcrossFiles.forEach((resource, filenames) -> {
                            if (isFlatImport) {
                                errorMessage.append(String.format(DUPLICATES_ACROSS_FILES_FLAT_IMPORT,
                                        resourceType, resource.name, resource.version, filenames));
                            } else {
                                errorMessage.append(String.format(DUPLICATES_ACROSS_FILES_NON_FLAT_IMPORT,
                                        resourceType, resource.name, resource.version, resource.folder, filenames));
                            }
                        }
                );
            }
            throw new IllegalArgumentException(errorMessage.toString().trim());
        }
    }

    private Map<String, Set<ResourceNameAndVersionAndPath>> findSameResourcesWithinSameFiles(Map<String, List<ResourceNameAndVersionAndPath>> fileNameToListResources) {
        var filesWithDuplicateApplications = new HashMap<String, Set<ResourceNameAndVersionAndPath>>();

        for (var entry : fileNameToListResources.entrySet()) {
            var filename = entry.getKey();
            var resources = entry.getValue();

            if (CollectionUtils.isEmpty(resources)) {
                continue;
            }
            var duplicatedResourceNames = getDuplicateResourceNamesAndPath(resources);

            if (!duplicatedResourceNames.isEmpty()) {
                filesWithDuplicateApplications.put(filename, duplicatedResourceNames);
            }
        }
        return filesWithDuplicateApplications;
    }

    private Map<ResourceNameAndVersionAndPath, Set<String>> findSameResourcesWithinDifferentFiles(Map<String, List<ResourceNameAndVersionAndPath>> fileNameToListResources) {
        var resourceToFilenamesMap = new HashMap<ResourceNameAndVersionAndPath, Set<String>>();

        for (var entry : fileNameToListResources.entrySet()) {
            var filename = entry.getKey();
            var resources = entry.getValue();

            if (CollectionUtils.isEmpty(resources)) {
                continue;
            }

            for (var resource : resources) {
                resourceToFilenamesMap
                        .computeIfAbsent(resource, k -> new HashSet<>())
                        .add(filename);
            }
        }
        return resourceToFilenamesMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void checkResourcesExistence(Map<String, List<ResourceNameAndVersionAndPath>> fileNameToListResources,
                                        String resourceType) {
        var resourceTypeLower = resourceType.toLowerCase();
        if (fileNameToListResources.isEmpty()) {
            throw new IllegalArgumentException(String.format("No %s files (e.g., `%ss/*.json`) found or loaded from the archive. "
                            + "Please ensure %s files are placed in a `%ss/` directory and have a `.json` extension.",
                    resourceTypeLower, resourceTypeLower, resourceTypeLower, resourceTypeLower));
        }

        var noApplications = fileNameToListResources.values().stream()
                .filter(Objects::nonNull)
                .allMatch(List::isEmpty);
        if (noApplications) {
            throw new IllegalArgumentException(String.format("%s files (e.g., `%ss/*.json`) were found in the archive, "
                            + "but they do not contain %ss. Please verify the content of these files.",
                    resourceType, resourceTypeLower, resourceTypeLower));
        }
    }

    private static <T, I> Map<String, List<ResourceNameAndVersionAndPath>> toMapOfResourceNameAndVersionAndPath(
            Map<String, T> dtos,
            Function<T, List<I>> listExtractor,
            Function<I, ResourceNameAndVersionAndPath> mapper) {

        return dtos.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            T dto = entry.getValue();
                            List<I> items = (dto == null)
                                    ? List.of()
                                    : Optional.ofNullable(listExtractor.apply(dto)).orElse(List.of());
                            return items.stream()
                                    .map(mapper)
                                    .toList();
                        }
                ));
    }
}