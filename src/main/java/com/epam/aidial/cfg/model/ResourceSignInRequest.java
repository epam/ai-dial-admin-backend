package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSignInRequest {

    private String url;
    private CredentialsLevel credentialsLevel;
    private AuthenticationType authenticationType;
    private String code;
    private String apiKey;
    private String redirectUri;
}