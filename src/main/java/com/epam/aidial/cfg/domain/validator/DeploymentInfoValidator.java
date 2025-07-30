package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeploymentInfoValidator {

    public void validateDeploymentInfo(DeploymentInfoDto deploymentInfo, String containerId) {
        if (deploymentInfo == null) {
            throw new IllegalArgumentException("Container with ID '%s' not found".formatted(containerId));
        }

        String deploymentUrl = deploymentInfo.getUrl();
        if (StringUtils.isBlank(deploymentUrl)) {
            throw new IllegalArgumentException(
                "Container URL is not present, please check if it is deployed. Container ID: %s".formatted(containerId)
            );
        }
    }
}
