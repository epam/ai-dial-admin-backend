package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "openaiDeploymentsClient",
        url = "${core.client.url}",
        configuration = {
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        })
public interface OpenaiDeploymentsClient {

    @GetMapping(value = "/openai/toolsets/{toolSetName}")
    ToolSetDataDto getOpenaiToolSet(@PathVariable String toolSetName);

}