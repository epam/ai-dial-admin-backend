package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceSignOutRequestDto {
    @NotBlank(message = "resource url should be specified")
    private String url;

    @NotNull(message = "credentialsLevel should be specified")
    private CredentialsLevelDto credentialsLevel;

    @NotNull(message = "authenticationType should be specified")
    private AuthenticationTypeResourceDto authenticationType;
}