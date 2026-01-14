package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InterceptorDeploymentInfoDto extends DeploymentInfoDto {
    public InterceptorDeploymentInfoDto(String id, String name, String url) {
        super(id, name, url);
    }
}

