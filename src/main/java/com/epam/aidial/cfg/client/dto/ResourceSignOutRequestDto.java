package com.epam.aidial.cfg.client.dto;

import com.epam.aidial.cfg.dto.AuthenticationTypeDto;
import com.epam.aidial.cfg.dto.CredentialsLevelDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSignOutRequestDto {

    private String url;
    private CredentialsLevelDto credentialsLevel;
    private AuthenticationTypeDto authenticationType;
}