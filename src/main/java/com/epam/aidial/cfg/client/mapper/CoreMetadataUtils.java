package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.core.util.UrlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoreMetadataUtils {

    public static String extractPath(String path, @NotNull String prefix) {
        return decodePath(removeMetadataPrefix(path, prefix));
    }

    protected static String extractFolderId(String path, String prefix) {
        return decodePath(removeMetadataPrefix(removeName(path), prefix));
    }

    public static String removeMetadataPrefix(String path, String prefix) {
        return path.startsWith(prefix) ? path.substring(prefix.length()) : path;
    }

    public static String decodePath(String path) {
        return UrlUtil.decodePath(path);
    }

    public static String encodePath(String path) {
        var parts = path.split("/");

        return Stream.of(parts)
                .map(UrlUtil::encodePath)
                .collect(Collectors.joining("/"));
    }

    public static String encodeFolderPath(String path) {
        return encodePath(path) + "/";
    }

    private static String removeName(String path) {
        if (path == null) {
            return null;
        }
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("The metadata path does not contain a '/': %s".formatted(path));
        }
        return path.substring(0, lastSlashIndex);
    }

}
