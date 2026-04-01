package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceSignInRequestDto {
    @NotEmpty
    private String url;

    @NotNull(message = "Credentials level should be specified")
    private CredentialsLevelDto credentialsLevel;

    @NotNull(message = "Authentication type should be specified")
    private AuthenticationTypeResourceDto authenticationType;

    private String code;

    private String apiKey;

    private String redirectUri;
}