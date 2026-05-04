package com.epam.aidial.cfg.service;

public record ResourceNameAndVersionAndPath(String name, String version, String folder) {
    public static ResourceNameAndVersionAndPath from(
            String name,
            String version,
            String folder,
            boolean flatImport
    ) {
        return new ResourceNameAndVersionAndPath(
                name,
                version,
                flatImport ? null : folder
        );
    }
}