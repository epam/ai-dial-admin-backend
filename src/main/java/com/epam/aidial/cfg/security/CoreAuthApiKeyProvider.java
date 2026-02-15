package com.epam.aidial.cfg.security;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoreAuthApiKeyProvider implements AuthApiKeyProvider {

    private final String apiKey;

    @Override
    public String getAuthApiKey() {
        return apiKey;
    }
}
