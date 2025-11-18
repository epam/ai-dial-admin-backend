package com.epam.aidial.cfg.service.config.export;

import com.epam.aidial.cfg.service.config.impl.storage.ConfigSource;
import com.epam.aidial.cfg.service.config.transfer.ConfigTransferLock;
import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigExportServiceImpl implements ConfigExportService {

    private final ConfigSource configSource;
    private final ConfigTransferLock configTransferLock;

    @Override
    public void export(Config config, boolean createResources) {
        configTransferLock.withWriteLock(() -> configSource.writeConfig(config, createResources));
    }
}
