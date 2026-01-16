package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InferenceDeploymentInfoDto extends DeploymentInfoDto {
    public InferenceDeploymentInfoDto(String id, String name, String url) {
        super(id, name, url);
    }
}

