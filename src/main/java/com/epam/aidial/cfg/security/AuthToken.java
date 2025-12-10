package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.client.dto.TokenResponseDto;
import com.epam.aidial.cfg.utils.SecretUtils;

public record AuthToken(String accessToken, int expiresIn) {

    @Override
    public String toString() {
        return "AuthToken(accessToken=" + accessToken()
                + ", expiresIn=" + expiresIn()
                + ')';
    }

    public String toUnsecureString() {
        return "AuthToken(accessToken=" + SecretUtils.mask(accessToken())
                + ", expiresIn=" + expiresIn()
                + ')';
    }

    public static AuthToken from(TokenResponseDto dto) {
        if (dto == null) {
            return null;
        }
        return new AuthToken(dto.getAccessToken(), dto.getExpiresIn());
    }
}
