package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.service.impl.storage.ConfigSource;
import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigExportServiceImpl implements ConfigExportService {

    private final ConfigSource configSource;

    @Override
    public void export(Config config) {
        configSource.writeConfig(config);
    }
}
