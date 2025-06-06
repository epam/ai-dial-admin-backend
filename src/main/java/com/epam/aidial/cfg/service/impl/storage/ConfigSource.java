package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.core.config.Config;

public interface ConfigSource {

    Config readConfig();

    void writeConfig(Config configBody);

}