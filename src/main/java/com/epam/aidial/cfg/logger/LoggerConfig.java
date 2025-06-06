package com.epam.aidial.cfg.logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {

    @Bean
    public LoggerConfigSource configSourcePropertyFile(LoggerConfigProperties configClientProperties) {
        return new LoggerConfigSourceJsonFile(configClientProperties.getPath());
    }

    @Bean
    public ConfigUpdaterLoggerLevel configApplierLoggerLevel(LoggerConfigSourceJsonFile configSource) {
        return new ConfigUpdaterLoggerLevel(configSource);
    }

}
