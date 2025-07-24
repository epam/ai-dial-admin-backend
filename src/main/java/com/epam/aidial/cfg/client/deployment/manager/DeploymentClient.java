package com.epam.aidial.cfg.client.deployment.manager;

import com.epam.aidial.cfg.client.RetryClientConfiguration;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.DeploymentTypeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "deploymentClient",
        url = "${deployment.client.url}",
        configuration = {AuthorizationCoreClientConfiguration.class, RetryClientConfiguration.class}
)
public interface DeploymentClient {

    @GetMapping("/api/v1/deployments")
    List<DeploymentInfoDto> getDeployments(@RequestParam DeploymentTypeDto type);

    @GetMapping("/api/v1/deployments/{id}")
    DeploymentInfoDto getDeployment(@PathVariable String id);
}