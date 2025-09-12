package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ResourceAuthSettingsDto {

    @NotNull(message = "Authentication type must be defined")
    private AuthenticationTypeDto authenticationType = AuthenticationTypeDto.NONE;

    private String clientId;
    private String clientSecret;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String redirectUri;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String codeVerifier;
    private String apiKeyHeader;

    // TODO [VPA]: for ToolSet, remove globalAuthStatus, userLevelAuthStatus and appLevelAuthStatus (?)
    private ResourceAuthStatusDto globalAuthStatus;
    private ResourceAuthStatusDto userLevelAuthStatus;
    private ResourceAuthStatusDto appLevelAuthStatus;

    private List<String> scopesSupported;
}