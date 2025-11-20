package com.epam.aidial.cfg.service.config;

import org.jetbrains.annotations.Nullable;

public interface ConfigSyncErrorHandler {

    @Nullable
    String getPrefixedLastErrorMessage();
}
