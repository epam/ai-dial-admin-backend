package com.epam.aidial.cfg.service.config.reload;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class CoreConfigReloadCache {

    private final AtomicReference<Entry> cache = new AtomicReference<>();

    public void put(JsonNode config) {
        cache.set(new Entry(config, System.currentTimeMillis()));
    }

    public Entry get() {
        return cache.get();
    }

    public record Entry(JsonNode config, long reloadTimestamp) {
    }
}
