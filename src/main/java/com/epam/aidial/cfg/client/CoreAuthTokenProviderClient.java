package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.TokenResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "coreAuthTokenProviderClient",
        url = "${core.auth.token.provider.url}"
)
public interface CoreAuthTokenProviderClient {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenResponseDto getToken(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody String body
    );
}
