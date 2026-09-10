package com.epam.aidial.cfg.client;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "toolsClient",
        url = "${core.client.url}",
        configuration = {
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        })
public interface ToolsClient {

    @GetMapping(value = "/v1/toolset/{path}/tools")
    McpSchema.ListToolsResult getTools(@PathVariable String path, @RequestParam String nextCursor);

}