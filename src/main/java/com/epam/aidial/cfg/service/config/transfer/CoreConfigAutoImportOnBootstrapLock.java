package com.epam.aidial.cfg.service.config.transfer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(value = "config.import.autoImportOnBootstrap.enabled", havingValue = "true")
public class CoreConfigAutoImportOnBootstrapLock {

    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    public boolean isAutoImportFinished() {
        return atomicBoolean.get();
    }

    public void finishAutoImport() {
        atomicBoolean.set(true);
    }
}
