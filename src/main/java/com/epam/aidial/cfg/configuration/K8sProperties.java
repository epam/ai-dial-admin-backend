package com.epam.aidial.cfg.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kubernetes-config")
public class K8sProperties {
    private K8ConnectType connectType;

    private String masterUrl;
    private String oauthToken;
    private boolean trustCerts;

    private String namespace;

    private ClientConfig client;

    public enum K8ConnectType {
        CONFIG_FILE,
        TOKEN;
    }

    @Data
    public static class ClientConfig {
        private int maxConcurrentRequests;
        private int maxConcurrentRequestsPerHost;
        private int requestRetryBackoffLimit;
        private int requestTimeout;
        private int withWebsocketPingInterval;
        private int withWatchReconnectLimit;
        private int operationTimeoutMs;
    }
}
