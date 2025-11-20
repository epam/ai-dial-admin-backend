package com.epam.aidial.cfg.service.config.export;

import com.epam.aidial.core.config.Config;

public interface ConfigExportService {
    void export(Config config, boolean createResources);
}
