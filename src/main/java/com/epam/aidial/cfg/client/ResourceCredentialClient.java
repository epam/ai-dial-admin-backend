package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.client.dto.ResourceSignOutRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "resourceCredentialClient",
        url = "${core.client.url}",
        configuration = {
                MessageConversionCoreClientConfiguration.class,
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        })
public interface ResourceCredentialClient {

    /**
     * Implementation Details:
     *
     * <p>The response from the server does not include a Content-Type header. To handle this, a custom decoder was created
     * to facilitate the decoding of the response.
     *
     * <p>For more information, refer to the custom decoder implementation in:
     * {@link MessageConversionCoreClientConfiguration#feignDecoder(com.fasterxml.jackson.databind.ObjectMapper)}
     */

    @PostMapping("/v1/ops/toolset/signin")
    void signInToolSetResource(@RequestBody ResourceSignInRequestDto request);

    @PostMapping("/v1/ops/toolset/signout")
    void signOutToolSetResource(@RequestBody ResourceSignOutRequestDto request);

}