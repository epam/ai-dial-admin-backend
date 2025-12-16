package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class InterceptorDeploymentInfoDto extends DeploymentInfoDto {
    public InterceptorDeploymentInfoDto(UUID id, String name, String url) {
        super(id, name, url);
    }
}

