package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigSource;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigUtils;
import com.epam.aidial.cfg.service.config.transfer.ConfigTransferLock;
import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@LogExecution
public class CoreConfigRetrieverImpl implements CoreConfigRetriever {

    private final ConfigSource configSource;
    private final ConfigTransferLock configTransferLock;

    @Override
    public RawConfig getRawConfig(boolean addSecrets) {
        return configTransferLock.withReadLock(() -> {
            var rawConfigs = configSource.readRawConfig();
            return new RawConfig(rawConfigs, null);
        });
    }

    @Override
    public Config getConfig(boolean addSecrets) {
        return configTransferLock.withReadLock(() -> {
            var config = configSource.readConfig();
            if (!addSecrets) {
                ConfigUtils.removeSecrets(config);
            }
            return config;
        });
    }

}
