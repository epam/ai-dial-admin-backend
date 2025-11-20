package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.core.config.Config;

import java.util.Map;

public interface ConfigSource {

    Map<String, String> readRawConfig();

    Config readConfig();

    void writeConfig(Config configBody, boolean createResources);

}