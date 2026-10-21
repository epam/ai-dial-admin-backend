package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ApplicationDeploymentInfoDto extends DeploymentInfoDto {
    public ApplicationDeploymentInfoDto(String id, String displayName, String url) {
        super(id, displayName, url);
    }
}
