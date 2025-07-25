package com.epam.aidial.cfg.client.deployment.manager;

import com.epam.aidial.cfg.client.RetryClientConfiguration;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "deploymentClient",
        url = "${plugins.deployment.manager.client.url}",
        configuration = RetryClientConfiguration.class
)
public interface DeploymentManagerClient {

    @GetMapping("/api/v1/deployments/{id}")
    DeploymentInfoDto getDeployment(@PathVariable String id);
}