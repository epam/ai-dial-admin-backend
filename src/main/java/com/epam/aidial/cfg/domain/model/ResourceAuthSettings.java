package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.utils.SecretUtils;
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

    private List<String> scopesSupported;
    private String tokenEndpointAuthMethod;

    @Override
    public String toString() {
        return "ResourceAuthSettings(" + "authenticationType=" + getAuthenticationType()
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