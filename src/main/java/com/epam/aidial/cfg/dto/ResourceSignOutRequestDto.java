package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceSignOutRequestDto {
    @NotBlank(message = "Resource url should be specified")
    private String url;

    @NotNull(message = "Credentials level should be specified")
    private CredentialsLevelDto credentialsLevel;

    @NotNull(message = "Authentication type should be specified")
    private AuthenticationTypeResourceDto authenticationType;
}