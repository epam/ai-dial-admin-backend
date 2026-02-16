package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.UserBucketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "bucketClient",
        url = "${core.client.url}",
        configuration = {
                AuthorizationCoreClientConfiguration.class
        }
)
public interface BucketClient {

    @GetMapping("/v1/bucket")
    UserBucketDto getBucket();
}