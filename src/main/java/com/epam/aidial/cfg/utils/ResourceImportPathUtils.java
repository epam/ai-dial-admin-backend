package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ResourceImportPath;
import org.apache.commons.lang3.StringUtils;

public final class ResourceImportPathUtils {

    public static final String PROMPTS_FOLDER = "prompts/";
    public static final String PUBLIC_FOLDER = "public/";

    private ResourceImportPathUtils() {
    }

    public static ResourceImportPath resolveVersionedEximImportPaths(ImportResources importResources,
                                                                     String versionedItemName,
                                                                     String folderId) {
        String targetPath;
        if (importResources.isFlatImport()) {
            targetPath = importResources.getPath() + "/" + versionedItemName;
        } else {
            var folderPathWithoutPublic = StringUtils.removeStart(folderId, PUBLIC_FOLDER);
            targetPath = importResources.getPath() + "/" + folderPathWithoutPublic + versionedItemName;
        }
        var sourcePath = folderId == null
                ? versionedItemName
                : StringUtils.stripEnd(folderId, "/") + "/" + versionedItemName;
        return new ResourceImportPath(sourcePath, targetPath);
    }

    public static ResourceImportPath resolvePromptImportPaths(ImportResources importPrompts, String promptId) {
        var sourcePath = StringUtils.removeStart(promptId, PROMPTS_FOLDER);
        String targetPath;
        if (importPrompts.isFlatImport()) {
            var promptName = PathUtils.parseVersionedPath(sourcePath).getVersionedName();
            targetPath = importPrompts.getPath() + "/" + promptName;
        } else {
            var sourcePathWithoutPublic = StringUtils.removeStart(sourcePath, PUBLIC_FOLDER);
            targetPath = importPrompts.getPath() + "/" + sourcePathWithoutPublic;
        }
        return new ResourceImportPath(sourcePath, targetPath);
    }

    public static ResourceImportPath resolveFileZipImportPaths(String rootPathStripped,
                                                               String fileName,
                                                               boolean flatImport,
                                                               String filesZipPrefix) {
        var sourcePath = StringUtils.removeStart(fileName, filesZipPrefix);
        String targetPath;
        if (flatImport) {
            var fileNameOnly = PathUtils.parsePath(fileName).getName();
            targetPath = rootPathStripped + "/" + fileNameOnly;
        } else {
            var sourcePathWithoutPublic = StringUtils.removeStart(sourcePath, PUBLIC_FOLDER);
            targetPath = rootPathStripped + "/" + sourcePathWithoutPublic;
        }
        return new ResourceImportPath(sourcePath, targetPath);
    }
}