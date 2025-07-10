package com.epam.aidial.cfg.client;

import com.epam.aidial.core.config.Config;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "configClient", url = "${core.client.url}", configuration = AuthorizationCoreClientConfiguration.class)
public interface CoreConfigClient {

    @PostMapping("/v1/ops/config/reload")
    Config reload();

}
