package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateStatusResolver;
import com.epam.aidial.cfg.utils.json.JsonNodeComparator;
import com.epam.aidial.core.config.CoreModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
public class SyncStateConfiguration {

    @Bean
    public EntitySyncStateStatusResolver entitySyncStateStatusResolver(
            @Value("${config.export.sync.duration.thresholdMs}") long thresholdMs) {
        Map<Class<?>, JsonNodeComparator> customJsonNodeComparators = Map.of(
                CoreModel.class, new JsonNodeComparator(Set.of("upstreams"))
        );
        return new EntitySyncStateStatusResolver(customJsonNodeComparators, thresholdMs);
    }
}
