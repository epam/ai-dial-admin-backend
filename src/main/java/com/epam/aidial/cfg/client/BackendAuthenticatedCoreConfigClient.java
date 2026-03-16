package com.epam.aidial.cfg.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "backendAuthenticatedConfigClient",
        url = "${core.client.url}",
        configuration = {
                BackendAuthenticatedCoreClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        }
)
public interface BackendAuthenticatedCoreConfigClient {

    @PostMapping("/v1/ops/config/reload")
    JsonNode reload();
}
