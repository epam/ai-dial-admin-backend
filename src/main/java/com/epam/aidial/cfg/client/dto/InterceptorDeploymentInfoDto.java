package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InterceptorDeploymentInfoDto extends DeploymentInfoDto {
    public InterceptorDeploymentInfoDto(String id, String displayName, String url) {
        super(id, displayName, url);
    }
}

