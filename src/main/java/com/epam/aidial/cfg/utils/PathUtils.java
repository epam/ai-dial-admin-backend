package com.epam.aidial.cfg.utils;

import com.epam.aidial.core.util.UrlUtil;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

@UtilityClass
public class PathUtils {

    public static boolean isAnyPathSegmentEndsWithDot(String path) {
        var segments = path.split("/");
        return Arrays.stream(segments)
                .anyMatch(segment -> segment.endsWith("."));
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
