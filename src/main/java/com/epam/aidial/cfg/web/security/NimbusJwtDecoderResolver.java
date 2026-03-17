package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public interface NimbusJwtDecoderResolver {

    NimbusJwtDecoder resolve(JwtProviderConfig config);
}
