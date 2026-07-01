package com.epam.aidial.cfg.client.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InferenceDeploymentInfoDto extends DeploymentInfoDto {
    private InferenceTask inferenceTask;

    public InferenceDeploymentInfoDto(String id, String name, String url, InferenceTask inferenceTask) {
        super(id, name, url);
        this.inferenceTask = inferenceTask;
    }
}
