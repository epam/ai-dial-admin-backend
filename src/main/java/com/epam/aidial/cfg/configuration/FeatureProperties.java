package com.epam.aidial.cfg.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "features")
public class FeatureProperties {

    private Map<String, Boolean> flags;

}