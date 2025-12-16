package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class NimDeploymentInfoDto extends DeploymentInfoDto {
    public NimDeploymentInfoDto(UUID id, String name, String url) {
        super(id, name, url);
    }
}

