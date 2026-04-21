package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.ResourceNodeInfo;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ExportPathUtils {

    public static final String DIAL_FOLDER_FILE = ".dial_folder";
    private static final String PUBLIC_FOLDER = "public/";

    /**
     * Excludes the folder marker file. Prompts, applications, and toolsets store it as a versioned
     * resource (e.g. {@code .dial_folder__1.0.0}), not only as {@code .dial_folder}.
     */
    public static boolean isTechnicalItem(String fullPath) {
        var fileName = PathUtils.parsePath(fullPath).getName();
        if (DIAL_FOLDER_FILE.equals(fileName)) {
            return true;
        }
        return fileName.startsWith(DIAL_FOLDER_FILE + "__");
    }

    /**
     * Collects storage paths of exportable items ({@link #isTechnicalItem}).
     * Only direct child items of a folder (flat, one level).
     */
    public static Set<String> collectExportablePaths(ResourceNodeInfo<?> node) {
        if (node == null || node.getNodeType() == null) {
            return Collections.emptySet();
        }
        if (node.getNodeType() == NodeType.ITEM) {
            var path = node.getPath();
            if (path == null || isTechnicalItem(path)) {
                return Collections.emptySet();
            }
            return Set.of(path);
        }
        if (node.getNodeType() == NodeType.FOLDER) {
            var items = node.getItems();
            if (items == null) {
                return Collections.emptySet();
            }
            return items.stream()
                    .filter(i -> i.getNodeType() == NodeType.ITEM)
                    .map(ResourceNodeInfo::getPath)
                    .filter(path -> !isTechnicalItem(path))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * Path under the export archive for an item when the user selected a folder to export
     * (last folder segment becomes the new root under {@code public/}, inner structure preserved).
     */
    public static String toFolderExportPublicPath(String itemStoragePath, String exportedFolderPath) {
        var folderName = PathUtils.folderNameWithoutPath(exportedFolderPath);
        var archiveFolderPath = PUBLIC_FOLDER + folderName;
        var pathParts = PathUtils.parsePath(itemStoragePath);
        var insideFolder = pathParts.getFolderId().substring(exportedFolderPath.length());
        return archiveFolderPath + insideFolder + pathParts.getName();
    }

    /**
     * Flat path for a single exported file: {@code public/} + last path segment (file name).
     */
    public static String toSingleFileExportPublicPath(String fileStoragePath) {
        return PUBLIC_FOLDER + PathUtils.parsePath(fileStoragePath).getName();
    }

    /**
     * Flat path for a single versioned exported resource: {@code public/} + versioned name ({@code name__version}).
     */
    public static String toSingleVersionedResourceExportPublicPath(String promptStoragePath) {
        return PUBLIC_FOLDER + PathUtils.parseVersionedPath(promptStoragePath).getVersionedName();
    }

    /**
     * Rewrites storage paths for exported versioned resources (prompts, applications, toolsets):
     * single selection → {@link #toSingleVersionedResourceExportPublicPath}; folder selection → {@link #toFolderExportPublicPath}.
     */
    public static String toExportedVersionedStoragePath(String storagePath, String exportFolderPath) {
        if (exportFolderPath == null) {
            return toSingleVersionedResourceExportPublicPath(storagePath);
        }
        return toFolderExportPublicPath(storagePath, exportFolderPath);
    }

    /**
     * Rewrites storage paths for exported files:
     * single selection → {@link #toSingleFileExportPublicPath}; folder selection → {@link #toFolderExportPublicPath}.
     */
    public static String toExportedFileStoragePath(String storagePath, String exportFolderPath) {
        if (exportFolderPath == null) {
            return toSingleFileExportPublicPath(storagePath);
        }
        return toFolderExportPublicPath(storagePath, exportFolderPath);
    }
}