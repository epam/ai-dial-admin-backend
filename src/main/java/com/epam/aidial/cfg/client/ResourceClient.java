package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.MoveResourceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "resourceClient", url = "${core.client.url}", configuration = AuthorizationCoreClientConfiguration.class)
public interface ResourceClient {

    @PostMapping("/v1/ops/resource/move")
    void move(@RequestBody MoveResourceDto movePromptDto);
}
