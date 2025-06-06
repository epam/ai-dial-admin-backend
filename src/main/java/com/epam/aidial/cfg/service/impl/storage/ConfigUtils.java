package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.core.config.Config;

public class ConfigUtils {
    public static Config secretsConfig(Config config) {
        Config secretConfig = new Config();
        secretConfig.setKeys(config.getKeys());
        secretConfig.setModels(config.getModels());
        return secretConfig;
    }
}
