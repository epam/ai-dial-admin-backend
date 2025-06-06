package com.epam.aidial.cfg.configuration.logging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "com.epam.aidial.cfg.configuration.customizable-trace-interceptor")
@RequiredArgsConstructor
@Setter
@Getter
public class CustomizableTraceInterceptorProperties {

    private Map<MessageType, String> messages = new HashMap<>();

    @RequiredArgsConstructor
    public enum MessageType {
        ENTER, EXIT, EXCEPTION
    }
}
