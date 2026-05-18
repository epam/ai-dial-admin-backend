package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.DeploymentDataDto;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "deploymentClient",
        url = "${core.client.url}",
        configuration = {
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        }
)
public interface DeploymentClient {

    @GetMapping("/v1/deployments/{deploymentName}/configuration")
    Map<String, Object> getConfiguration(@PathVariable String deploymentName);

    @GetMapping("/v1/deployments")
    List<DeploymentDataDto> listDeployments(
            @RequestParam(value = "interface_type", required = false) List<String> interfaceTypes);

    @GetMapping(value = "/openai/toolsets/{toolSetName}")
    ToolSetDataDto getToolSet(@PathVariable String toolSetName);
}