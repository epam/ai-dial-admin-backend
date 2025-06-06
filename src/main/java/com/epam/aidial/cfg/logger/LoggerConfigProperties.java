package com.epam.aidial.cfg.logger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "logger.configuration")
@RequiredArgsConstructor
@Getter
@Setter
public class LoggerConfigProperties {

    private String path;

}
