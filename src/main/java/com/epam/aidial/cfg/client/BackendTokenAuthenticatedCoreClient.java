package com.epam.aidial.cfg.client;

import com.epam.aidial.core.config.Config;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "backendTokenAuthenticatedCoreClient",
        url = "${core.client.url}",
        configuration = {
                CoreAuthTokenProviderConfiguration.class,
                FeignErrorDecoderConfiguration.class
        }
)
public interface BackendTokenAuthenticatedCoreClient {

    @PostMapping("/v1/ops/config/reload")
    Config reload();

}
