package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AdapterDeploymentInfoDto extends DeploymentInfoDto {
    public AdapterDeploymentInfoDto(String id, String name, String url) {
        super(id, name, url);
    }
}

