package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class AdapterDeploymentInfoDto extends DeploymentInfoDto {
    public AdapterDeploymentInfoDto(UUID id, String name, String url) {
        super(id, name, url);
    }
}

