package com.epam.aidial.cfg.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImportResourcesResult {
    private final String sourcePath;
    private final String targetPath;
    private final ImportResourcesStatus status;
    private final String error;

    public static ImportResourcesResult createSuccess(String sourcePath, String targetPath) {
        return new ImportResourcesResult(sourcePath, targetPath, ImportResourcesStatus.SUCCESS, null);
    }

    public static ImportResourcesResult createFailure(String sourcePath, String targetPath, String error) {
        return new ImportResourcesResult(sourcePath, targetPath, ImportResourcesStatus.FAILURE, error);
    }

    public static ImportResourcesResult createAlreadyExists(String sourcePath, String targetPath) {
        return new ImportResourcesResult(sourcePath, targetPath, ImportResourcesStatus.ALREADY_EXISTS, null);
    }

    public static ImportResourcesResult createSkip(String sourcePath, String targetPath) {
        return new ImportResourcesResult(sourcePath, targetPath, ImportResourcesStatus.SKIPPED, null);
    }

    public boolean hasError() {
        return error != null;
    }

}