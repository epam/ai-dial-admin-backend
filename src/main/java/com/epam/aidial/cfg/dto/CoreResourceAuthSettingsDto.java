package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoreResourceAuthSettingsDto {
    private AuthenticationType authenticationType;
    private String clientId;
    private String clientSecret;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String redirectUri;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String codeVerifier;
    private String apiKeyHeader;
    private ResourceAuthStatus globalAuthStatus;
    private ResourceAuthStatus userLevelAuthStatus;
    private ResourceAuthStatus appLevelAuthStatus;
    private List<String> scopesSupported;

    public enum ResourceAuthStatus {
        SIGNED_IN,
        SIGNED_OUT,
        FAILED
    }

    public enum AuthenticationType {
        OAUTH,
        API_KEY,
        NONE
    }
}
