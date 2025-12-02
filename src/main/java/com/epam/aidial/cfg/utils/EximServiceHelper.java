package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.dto.ApplicationEximDto;

public class EximServiceHelper {
    public static String getVersionedName(String name, String version) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        return version == null || version.isBlank()
                ? name
                : name + "__" + version;
    }
    
    public static String getVersionedName(ApplicationEximDto dto) {
        return getVersionedName(dto.getName(), dto.getVersion());
    }
}