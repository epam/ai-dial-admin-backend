package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AdapterDeploymentInfoDto extends DeploymentInfoDto {
    public AdapterDeploymentInfoDto(String id, String displayName, String url) {
        super(id, displayName, url);
    }
}

