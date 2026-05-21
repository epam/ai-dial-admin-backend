package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
@Embeddable
public class ResourceAuthSettingsEntity {

    @Enumerated(EnumType.STRING)
    private AuthenticationTypeEntity authenticationType;

    private String clientId;
    private String clientSecret;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String redirectUri;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String codeVerifier;
    private String apiKeyHeader;

    private List<String> scopesSupported;
    private String tokenEndpointAuthMethod;
}