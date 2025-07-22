package com.epam.aidial.cfg.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "anonymousConfigClient", url = "${core.client.url}", configuration = RetryClientConfiguration.class)
public interface AnonymousCoreConfigClient {

    @GetMapping("/version")
    String getVersion();

}
