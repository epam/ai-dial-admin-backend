package com.epam.aidial.cfg.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class CoreConfigVersionNormalizer {

    public static String normalizeCoreVersion(String version) {
        if (version != null && version.contains("-")) {
            String croppedVersion = version.substring(0, version.indexOf('-'));
            log.info("Core config version normalized by changing from '{}' to '{}'", version, croppedVersion);
            return croppedVersion;
        }
        return version;
    }
}
