package com.epam.aidial.cfg.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "deploymentClient",
        url = "${core.client.url}",
        configuration = {
                AuthorizationCoreClientConfiguration.class
        }
)
public interface DeploymentClient {

    @GetMapping("/v1/deployments/{deploymentName}/configuration")
    Map<String, Object> getConfiguration(@PathVariable String deploymentName);
}
