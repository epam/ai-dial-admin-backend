package com.epam.aidial.cfg.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResourceAuthSettingsDto {
    private AuthenticationTypeDto authenticationType;
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
    private String tokenEndpointAuthMethod;

    public enum ResourceAuthStatus {
        SIGNED_IN,
        SIGNED_OUT,
        FAILED
    }
}