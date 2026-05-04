package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.utils.PathUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Validated
@Component
@LogExecution
@Slf4j
public class ResourceImportValidator {
    private static final String APPLICATION_RESOURCE = "Application";
    private static final String TOOLSET_RESOURCE = "ToolSet";
    private static final String DUPLICATES_WITHIN_FILES_FLAT_IMPORT = "\n    - File '%s' has duplicate %s: name '%s', version '%s'";
    private static final String DUPLICATES_WITHIN_FILES_NON_FLAT_IMPORT = DUPLICATES_WITHIN_FILES_FLAT_IMPORT + ", folder '%s'";
    private static final String DUPLICATES_ACROSS_FILES_FLAT_IMPORT = "\n    - %s with name '%s', version '%s' found in multiple files: %s";
    private static final String DUPLICATES_ACROSS_FILES_NON_FLAT_IMPORT = "\n    - %s with name '%s', version '%s', folder '%s found in multiple files: %s";

    public Map<ResourceNameAndVersionAndPath, String> collectApplicationUniquenessConflicts(
            boolean flatImport,
            @Valid ApplicationsEximDto applicationsEximDto
    ) {
        return collectUniquenessConflicts(
                flatImport,
                applicationsEximDto,
                ApplicationsEximDto::getApplications,
                application -> ResourceNameAndVersionAndPath.from(
                        application.getName(),
                        application.getVersion(),
                        application.getFolderId(),
                        flatImport
                ),
                APPLICATION_RESOURCE
        );
    }

    public Map<ResourceNameAndVersionAndPath, String> collectToolSetUniquenessConflicts(
            boolean flatImport,
            @Valid ToolSetsEximDto toolSetsEximDto
    ) {
        return collectUniquenessConflicts(
                flatImport,
                toolSetsEximDto,
                ToolSetsEximDto::getToolSets,
                toolset -> ResourceNameAndVersionAndPath.from(
                        toolset.getName(),
                        toolset.getVersion(),
                        toolset.getFolderId(),
                        flatImport
                ),
                TOOLSET_RESOURCE
        );
    }

    private <T, I> Map<ResourceNameAndVersionAndPath, String> collectUniquenessConflicts(
            boolean flatImport,
            T dto,
            Function<T, List<I>> extractor,
            Function<I, ResourceNameAndVersionAndPath> mapper,
            String resourceType
    ) {
        if (dto == null) {
            return Map.of();
        }

        var items = extractor.apply(dto);
        if (items == null || items.isEmpty()) {
            return Map.of();
        }

        var tuples = items.stream()
                .map(mapper)
                .toList();

        return collectNameVersionFolderUniquenessConflicts(
                tuples,
                flatImport,
                resourceType
        );
    }

    private Map<ResourceNameAndVersionAndPath, String> collectNameVersionFolderUniquenessConflicts(
            List<ResourceNameAndVersionAndPath> tuples,
            boolean flatImport,
            String resourceType) {
        var duplicateTuples = getDuplicateResourceNamesAndPath(tuples);
        if (duplicateTuples.isEmpty()) {
            return Map.of();
        }

        var errorsByKey = new LinkedHashMap<ResourceNameAndVersionAndPath, String>();

        for (var tuple : tuples) {
            if (duplicateTuples.contains(tuple)) {
                var message = formatDuplicateResourceInImportMessage(tuple, flatImport, resourceType);
                errorsByKey.putIfAbsent(tuple, message);
            }
        }
        return errorsByKey;
    }

    private String formatDuplicateResourceInImportMessage(ResourceNameAndVersionAndPath resource,
                                                          boolean isFlatImport,
                                                          String resourceType) {
        if (isFlatImport) {
            return "Duplicated %s name '%s' and version '%s' appears multiple times in the import file."
                    .formatted(resourceType.toLowerCase(), resource.name(), resource.version());
        }
        return "Duplicated %s name '%s' and version '%s' and folder '%s' appears multiple times in the import file."
                .formatted(resourceType.toLowerCase(), resource.name(), resource.version(), resource.folder());
    }

    public void validateFileImportInZip(ImportResources importFiles, MultipartFile zipFile) throws IOException {
        var resources = getFileNamesFromZip(zipFile);
        if (importFiles.isFlatImport()) {
            validateUniquenessFileNamesInFolders(resources);
        }
    }

    public Map<String, String> collectMultipartFilesUniquenessConflicts(List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            return Map.of();
        }
        var nonEmpty = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();
        var nameToCount = nonEmpty.stream()
                .collect(Collectors.groupingBy(f -> Objects.toString(f.getOriginalFilename(), ""), Collectors.counting()));
        var duplicatedNames = nameToCount.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (duplicatedNames.isEmpty()) {
            return Map.of();
        }
        var errorsByKey = new LinkedHashMap<String, String>();
        for (var name : duplicatedNames) {
            var message = "Duplicated file name '%s' appears multiple times in the import request."
                    .formatted(name);
            errorsByKey.put(name, message);
        }
        return errorsByKey;
    }

    public String multipartFileUniquenessKey(MultipartFile file) {
        return Objects.toString(file.getOriginalFilename(), "");
    }

    private List<ResourceNameAndVersionAndPath> getFileNamesFromZip(MultipartFile zipFile) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry zipEntry;
            var names = new ArrayList<ResourceNameAndVersionAndPath>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    throw new IllegalArgumentException(String.format("Invalid zip format for file '%s'", zipFile.getOriginalFilename()));
                }
                String zipEntryName = zipEntry.getName();
                try {
                    zipEntryName = PathUtils.validateZipEntryPath(zipEntryName);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", zipEntryName, e);
                    continue;
                }
                var parts = PathUtils.parsePath(zipEntryName);
                names.add(new ResourceNameAndVersionAndPath(parts.getName(), null, parts.getFolderId()));
            }
            return names;
        }
    }

    private void validateUniquenessFileNamesInFolders(List<ResourceNameAndVersionAndPath> resources) {
        Map<String, Set<String>> nameToFolders = resources.stream()
                .collect(Collectors.groupingBy(
                        ResourceNameAndVersionAndPath::name,
                        Collectors.mapping(ResourceNameAndVersionAndPath::folder, Collectors.toSet())));

        Map<String, Set<String>> duplicated = nameToFolders.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (duplicated.isEmpty()) {
            return;
        }

        var errorMessage = new StringBuilder("Files uniqueness violation. Conflicts found:");
        duplicated.forEach((name, folders) ->
                errorMessage.append("\n - Duplicated file")
                        .append(" name '").append(name).append("' found in folders: ")
                        .append(String.join(", ", folders)));
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
                                filename, resourceType.toLowerCase(), resource.name(), resource.version()));
                    } else {
                        errorMessage.append(String.format(DUPLICATES_WITHIN_FILES_NON_FLAT_IMPORT,
                                filename, resourceType.toLowerCase(), resource.name(), resource.version(), resource.folder()));
                    }
                }));
            }

            if (!duplicatesAcrossFiles.isEmpty()) {
                errorMessage.append(String.format("\n  %ss shared across different files:", resourceType));
                duplicatesAcrossFiles.forEach((resource, filenames) -> {
                            if (isFlatImport) {
                                errorMessage.append(String.format(DUPLICATES_ACROSS_FILES_FLAT_IMPORT,
                                        resourceType, resource.name(), resource.version(), filenames));
                            } else {
                                errorMessage.append(String.format(DUPLICATES_ACROSS_FILES_NON_FLAT_IMPORT,
                                        resourceType, resource.name(), resource.version(), resource.folder(), filenames));
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