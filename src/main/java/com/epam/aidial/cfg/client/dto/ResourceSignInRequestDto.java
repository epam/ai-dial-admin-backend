package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSignInRequestDto {

    private String url;
    private CredentialsLevel credentialsLevel;
    private AuthenticationType authenticationType;
    private String code;
    private String apiKey;
}