package com.epam.aidial.cfg.dto;

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
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String redirectUri;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String codeVerifier;
    private String apiKeyHeader;

    private List<String> scopesSupported;

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
                + ", scopesSupported=" + getScopesSupported()
                + ')';
    }
}