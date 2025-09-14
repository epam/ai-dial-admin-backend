package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.PromptDto;
import com.epam.aidial.cfg.client.dto.PromptMetadataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "promptClient",
        url = "${core.client.url}",
        configuration = {
                MessageConversionCoreClientConfiguration.class,
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
        }
)
public interface PromptClient {

    @GetMapping("/v1/metadata/prompts/{path}")
    PromptMetadataDto getPromptsMetadata(
            @PathVariable String path,
            @RequestParam boolean recursive,
            @RequestParam String token,
            @RequestParam int limit
    );

    /**
     * Implementation Details:
     *
     * <p>The response from the server does not include a Content-Type header. To handle this, a custom decoder was created
     * to facilitate the decoding of the response.
     *
     * <p>For more information, refer to the custom decoder implementation in:
     * {@link MessageConversionCoreClientConfiguration#feignDecoder(org.springframework.beans.factory.ObjectFactory, com.fasterxml.jackson.databind.ObjectMapper)}
     */
    @GetMapping("/v1/prompts/{path}")
    PromptDto getPrompt(
            @PathVariable String path
    );

    @PutMapping("/v1/prompts/{path}")
    PromptMetadataDto createPrompt(
            @PathVariable String path,
            @RequestBody PromptDto promptDto,
            @RequestHeader Map<String, String> headers
    );

    @DeleteMapping("/v1/prompts/{path}")
    void deletePrompt(@PathVariable String path);

}
