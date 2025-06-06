package com.epam.aidial.cfg.configuration;

import com.epam.aidial.core.config.Storage;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "core")
public class CoreConfigurationProperties {

    private Storage storage;

    private Map<String, Object> resources;
}
