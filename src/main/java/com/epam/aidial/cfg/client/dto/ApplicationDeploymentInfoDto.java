package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ApplicationDeploymentInfoDto extends DeploymentInfoDto {
    public ApplicationDeploymentInfoDto(String id, String name, String url) {
        super(id, name, url);
    }
}
