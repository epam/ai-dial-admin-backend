package com.epam.aidial.core.config;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreResourceAuthSettings {

    @NotNull(message = "Authentication type must be defined")
    @JsonAlias({"authenticationType", "authentication_type"})
    @Builder.Default
    private CoreAuthenticationType authenticationType = CoreAuthenticationType.NONE;

    @JsonAlias({"clientId", "client_id"})
    private String clientId;

    @JsonAlias({"clientSecret", "client_secret"})
    private String clientSecret;

    @JsonAlias({"authorizationEndpoint", "authorization_endpoint"})
    private String authorizationEndpoint;

    @JsonAlias({"tokenEndpoint", "token_endpoint"})
    private String tokenEndpoint;

    @JsonAlias({"redirectUri", "redirect_uri"})
    private String redirectUri;

    @JsonAlias({"codeChallenge", "code_challenge"})
    private String codeChallenge;

    @JsonAlias({"codeChallengeMethod", "code_challenge_method"})
    private String codeChallengeMethod;

    @JsonAlias({"codeVerifier", "code_verifier"})
    private String codeVerifier;

    @JsonAlias({"apiKeyHeader", "api_key_header"})
    private String apiKeyHeader;

    @JsonAlias({"scopesSupported", "scopes_supported"})
    private List<String> scopesSupported;

    @NotNull(message = "Authentication method must be defined")
    @JsonAlias({"tokenEndpointAuthMethod", "token_endpoint_auth_method"})
    private String tokenEndpointAuthMethod; // 0.44.0

    @Override
    public String toString() {
        return "CoreResourceAuthSettings(" + "authenticationType=" + getAuthenticationType()
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
                + ", tokenEndpointAuthMethod=" + getScopesSupported()
                + ')';
    }

    @JsonIgnore
    public static CoreResourceAuthSettings empty() {
        CoreResourceAuthSettings coreResourceAuthSettings = new CoreResourceAuthSettings();

        coreResourceAuthSettings.setAuthenticationType(null);

        return coreResourceAuthSettings;
    }
}