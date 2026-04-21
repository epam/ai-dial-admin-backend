package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceNodeInfo;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@UtilityClass
public final class ResourceEximExportHelper {

    public static void addExportEntry(Map<String, String> entries, String storagePath, String exportFolderPath) {
        if (entries.containsKey(storagePath)) {
            throw new IllegalStateException("Duplicate entry for path: " + storagePath);
        }
        entries.put(storagePath, exportFolderPath);
    }

    /**
     * Builds {@code storagePath → export folder path (or {@code null} for single-item export)} from user paths.
     *
     * @param collectPathsUnderFolder returns item paths under a folder path (recursive metadata request)
     */
    public static Map<String, String> resolveExportEntries(
            List<String> paths,
            Function<String, Set<String>> collectPathsUnderFolder) {
        Map<String, String> entries = new LinkedHashMap<>();
        for (String path : paths) {
            if (PathUtils.isFolderPath(path)) {
                for (String itemPath : collectPathsUnderFolder.apply(path)) {
                    addExportEntry(entries, itemPath, path);
                }
            } else if (!ExportPathUtils.isTechnicalItem(path)) {
                addExportEntry(entries, path, null);
            }
        }
        return entries;
    }

    /**
     * Loads flat item paths under a folder via recursive metadata; returns empty set if not found.
     */
    public static Set<String> collectPathsUnderFolder(
            String folderPath,
            Function<ResourceMetadataRequest, ? extends ResourceNodeInfo<?>> fetchMetadata,
            String resourceKind) {
        try {
            var request = ResourceMetadataRequest.builder()
                    .path(folderPath)
                    .recursive(true)
                    .build();
            var node = fetchMetadata.apply(request);
            return ExportPathUtils.collectExportablePaths(node);
        } catch (ResourceNotFoundException e) {
            log.debug("Path not found for {} export: {}", resourceKind, folderPath, e);
            return Collections.emptySet();
        }
    }
}