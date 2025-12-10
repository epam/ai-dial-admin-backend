package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.client.CoreAuthTokenProviderClient;
import com.epam.aidial.cfg.utils.SecretUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Slf4j
public class CoreAuthTokenProvider implements AuthTokenProvider {

    private final CoreAuthTokenProviderClient client;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    public CoreAuthTokenProvider(CoreAuthTokenProviderClient client, String clientId, String clientSecret, String scope) {
        this.client = client;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    @Override
    public AuthToken getAuthToken() {
        try {
            var token = AuthToken.from(client.getToken(
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    AuthorizationGrantType.CLIENT_CREDENTIALS.getValue(),
                    clientId,
                    clientSecret,
                    scope
            ));
            if (token == null) {
                log.warn("Failed to obtain access token: Token is null for getToken(clientId={}, clientSecret='{}', scope='{}')",
                        clientId,
                        SecretUtils.mask(clientSecret),
                        scope
                );
                throw new AccessDeniedException("Failed to obtain access token");
            }

            logGetToken(token);
            return token;
        } catch (Exception e) {
            log.warn("Error obtaining access token for getToken(clientId={}, clientSecret='{}', scope='{}')",
                    clientId,
                    SecretUtils.mask(clientSecret),
                    scope,
                    e
            );
            throw new AccessDeniedException("Failed to obtain access token", e);
        }
    }

    private void logGetToken(AuthToken token) {
        if (log.isTraceEnabled()) {
            log.trace("getToken(clientId={}, clientSecret='{}', scope='{}')={}", clientId, clientSecret, scope, token.toUnsecureString());
        } else {
            log.debug("getToken(clientId={}, clientSecret='{}', scope='{}')={}", clientId, SecretUtils.mask(clientSecret), scope, token);
        }
    }
}
