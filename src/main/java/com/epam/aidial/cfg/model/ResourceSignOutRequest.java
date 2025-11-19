package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSignOutRequest {

    private String url;
    private CredentialsLevel credentialsLevel;
    private AuthenticationType authenticationType;
}