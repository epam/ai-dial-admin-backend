package com.epam.aidial.cfg.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ResourceAuthSettings {

    @NotNull(message = "Authentication type must be defined")
    private AuthenticationType authenticationType = AuthenticationType.NONE;

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
}