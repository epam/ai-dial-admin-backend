package com.epam.aidial.cfg.service.transfer.exporter;

import com.epam.aidial.core.config.Config;

import java.util.Map;

public interface CoreConfigRetriever {

    record RawConfig(Map<String, String> configs, Map<String, String> secrets) {
    }

    RawConfig getRawConfig(boolean addSecrets);

    Config getConfig(boolean addSecrets);

}
