package com.epam.aidial.cfg.service;

public record ResourceLocation(String name, String version, String folder) {
    public static ResourceLocation from(
            String name,
            String version,
            String folder,
            boolean flatImport
    ) {
        return new ResourceLocation(
                name,
                version,
                flatImport ? null : folder
        );
    }
}