package com.epam.aidial.cfg.service.config.transfer.version;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class CoreConfigAutoDetectedVersionCache {

    private final AtomicReference<String> versionCache = new AtomicReference<>();

    public void putVersion(String version) {
        versionCache.set(version);
    }

    public String getVersion() {
        return versionCache.get();
    }
}
