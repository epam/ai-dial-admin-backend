package com.epam.aidial.cfg.utils;

import com.epam.aidial.core.util.UrlUtil;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@UtilityClass
public class PathUtils {

    public static boolean isAnyPathSegmentEndsWithDot(String path) {
        var segments = path.split("/");
        return Arrays.stream(segments).anyMatch(segment -> segment.endsWith("."));
    }

    public static boolean isPathParseable(String path) {
        return path.indexOf('/') != path.length() - 1;
    }

    public static String trimTrailingSlash(String path) {
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String ensureTrailingSlash(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.endsWith("/") ? path : path + "/";
    }

    public static PathParts parsePath(String path) {
        path = trimTrailingSlash(path);
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("The path does not contain a '/': %s".formatted(path));
        }

        var folderId = path.substring(0, lastSlashIndex + 1);
        var name = path.substring(lastSlashIndex + 1);

        return PathParts.builder()
                .path(path)
                .folderId(folderId)
                .name(name)
                .build();
    }

    public static VersionedPathParts parseEncodedVersionedPath(String path, String prefix) {
        var pathWithoutPrefix = path.startsWith(prefix) ? path.substring(prefix.length()) : path;
        var pathDecoded = UrlUtil.decodePath(pathWithoutPrefix);

        return parseVersionedPath(pathDecoded);
    }

    public static VersionedPathParts parseVersionedPath(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("The path does not contain a '/': %s".formatted(path));
        }

        var folderId = path.substring(0, lastSlashIndex + 1);
        var rawName = path.substring(lastSlashIndex + 1);
        var nameAndVersion = extractNameAndVersion(rawName);

        return VersionedPathParts.builder()
                .path(path)
                .folderId(folderId)
                .name(nameAndVersion.getLeft())
                .version(nameAndVersion.getRight())
                .build();
    }

    private static Pair<String, String> extractNameAndVersion(String rawName) {
        var nameParts = rawName.split("__");
        String name;
        String version;
        if (nameParts.length != 2) {
            name = rawName;
            version = null;
        } else {
            name = nameParts[0];
            version = nameParts[1];
        }
        return Pair.of(name, version);
    }

    public static String buildPath(String folderId, String name, String version) {
        var cleanFolderId = StringUtils.stripEnd(folderId, "/");
        return cleanFolderId + "/" + name + "__" + version;
    }

    /**
     * Validates a zip entry path to prevent path traversal attacks.
     * Normalizes the path and ensures it doesn't escape the root directory.
     *
     * @param zipEntryPath the zip entry path to validate
     * @return the normalized path if valid
     * @throws IllegalArgumentException if path traversal is detected
     */
    public static String validateZipEntryPath(String zipEntryPath) {
        if (StringUtils.isBlank(zipEntryPath)) {
            throw new IllegalArgumentException("Zip entry path cannot be blank");
        }

        // Check for null bytes which can be used in path traversal attacks
        if (zipEntryPath.contains("\0")) {
            throw new IllegalArgumentException(String.format("Null byte detected in zip entry path: %s", zipEntryPath));
        }

        // Check for absolute paths BEFORE separator normalization
        // Unix-style absolute paths start with /, Windows-style start with drive letter (C:, D:, etc.)
        if (zipEntryPath.startsWith("/") || (zipEntryPath.length() >= 2 && zipEntryPath.charAt(1) == ':'
                && ((zipEntryPath.charAt(0) >= 'A' && zipEntryPath.charAt(0) <= 'Z')
                        || (zipEntryPath.charAt(0) >= 'a' && zipEntryPath.charAt(0) <= 'z')))) {
            throw new IllegalArgumentException(String.format("Path traversal detected in zip entry: %s (absolute path)", zipEntryPath));
        }

        // Normalize path separators (handle both / and \)
        String normalizedSeparator = zipEntryPath.replace('\\', '/');

        // Additional check for absolute paths after separator normalization (Unix-style)
        if (normalizedSeparator.startsWith("/")) {
            throw new IllegalArgumentException(String.format("Path traversal detected in zip entry: %s (absolute path)", zipEntryPath));
        }

        // Check for path traversal patterns in the ORIGINAL path BEFORE normalization
        // Path.normalize() resolves ../ sequences, so we must check before normalization
        if (normalizedSeparator.contains("../") || normalizedSeparator.contains("./") || normalizedSeparator.startsWith("../")
                || normalizedSeparator.startsWith("./") || normalizedSeparator.equals("..") || normalizedSeparator.endsWith("/..")) {
            throw new IllegalArgumentException(String.format("Path traversal detected in zip entry: %s", zipEntryPath));
        }

        // Use Path API to normalize and check for absolute paths (additional safety check)
        Path path = Paths.get(normalizedSeparator);

        // Double-check for absolute paths using Path API
        if (path.isAbsolute()) {
            throw new IllegalArgumentException(String.format("Path traversal detected in zip entry: %s (absolute path)", zipEntryPath));
        }

        Path normalizedPath = path.normalize();
        String normalizedString = normalizedPath.toString().replace('\\', '/');

        // Additional safety check: verify normalized path doesn't contain .. (shouldn't happen, but double-check)
        if (normalizedString.contains("../") || normalizedString.startsWith("../") || normalizedString.equals("..")) {
            throw new IllegalArgumentException(String.format("Path traversal detected in zip entry: %s (normalized: %s)", zipEntryPath, normalizedString));
        }

        return normalizedString;
    }

    @Data
    @Builder
    public class VersionedPathParts {
        private String path;
        private String folderId;
        private String name;
        private String version;

        public String getVersionedName() {
            return version == null ? name : name + "__" + version;
        }
    }

    @Data
    @Builder
    public class PathParts {
        private String path;
        private String folderId;
        private String name;
    }

}