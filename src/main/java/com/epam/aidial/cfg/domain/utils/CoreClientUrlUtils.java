package com.epam.aidial.cfg.domain.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoreClientUrlUtils {
    @Value("${core.client.url}")
    private String coreClientUrl;

    public String getNormalizedCoreClientUrl() {
        return coreClientUrl.endsWith("/")
                ? coreClientUrl.substring(0, coreClientUrl.length() - 1)
                : coreClientUrl;
    }
}