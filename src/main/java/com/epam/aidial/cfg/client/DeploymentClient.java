package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.dto.DeploymentTypeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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
}