package com.epam.aidial.cfg.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "backendTokenAuthenticatedCoreClient",
        url = "${core.client.url}",
        configuration = {
                CoreAuthTokenProviderClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        }
)
public interface BackendTokenAuthenticatedCoreClient {

    @PostMapping("/v1/ops/config/reload")
    JsonNode reload();
}
