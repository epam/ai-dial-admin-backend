package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import com.epam.aidial.cfg.utils.SecretUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ResourceAuthSettingsDto {

    @NotNull(message = "Authentication type must be defined")
    private AuthenticationTypeDto authenticationType = AuthenticationTypeDto.NONE;

    private String clientId;
    private String clientSecret;
    @Endpoint
    private String authorizationEndpoint;
    @Endpoint
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
    private TokenEndpointAuthMethodDto tokenEndpointAuthMethod = TokenEndpointAuthMethodDto.CLIENT_SECRET_BASIC;

    @Override
    public String toString() {
        return "ResourceAuthSettingsDto(" + "authenticationType=" + getAuthenticationType()
                + ", clientId='" + getClientId()
                + ", clientSecret='" + SecretUtils.mask(this.getClientSecret())
                + ", authorizationEndpoint='" + getAuthorizationEndpoint()
                + ", tokenEndpoint='" + getTokenEndpoint()
                + ", redirectUri='" + getRedirectUri()
                + ", codeChallenge='" + getCodeChallenge()
                + ", codeChallengeMethod='" + getCodeChallengeMethod()
                + ", codeVerifier='" + getCodeVerifier()
                + ", apiKeyHeader='" + getApiKeyHeader()
                + ", globalAuthStatus=" + getGlobalAuthStatus()
                + ", userLevelAuthStatus=" + getUserLevelAuthStatus()
                + ", appLevelAuthStatus=" + getAppLevelAuthStatus()
                + ", scopesSupported=" + getScopesSupported()
                + ", tokenEndpointAuthMethod" + getTokenEndpointAuthMethod()
                + ')';
    }

    public enum ResourceAuthStatus {
        SIGNED_IN,
        SIGNED_OUT,
        FAILED
    }
}