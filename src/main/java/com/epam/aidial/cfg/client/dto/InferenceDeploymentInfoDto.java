package com.epam.aidial.cfg.client.dto;

import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class InferenceDeploymentInfoDto extends DeploymentInfoDto {
    public InferenceDeploymentInfoDto(UUID id, String name, String url) {
        super(id, name, url);
    }
}

