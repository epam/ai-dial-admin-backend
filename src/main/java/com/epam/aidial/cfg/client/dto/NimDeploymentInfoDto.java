package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NimDeploymentInfoDto extends DeploymentInfoDto {
    public NimDeploymentInfoDto(String id, String name, String url) {
        super(id, name, url);
    }
}

