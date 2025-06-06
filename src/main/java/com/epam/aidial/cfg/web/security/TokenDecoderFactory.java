package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.jwt.JwtDecoder;

public interface TokenDecoderFactory {

    JwtDecoder createJwtDecoder();
}
