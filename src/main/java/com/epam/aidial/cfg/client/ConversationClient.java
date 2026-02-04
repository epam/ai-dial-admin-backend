package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.ConversationDto;
import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "conversationClient",
        url = "${core.client.url}",
        configuration = {
                MessageConversionCoreClientConfiguration.class,
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        })
public interface ConversationClient {

    @GetMapping("/v1/metadata/conversations/{path}")
    ConversationMetadataDto getConversationMetadata(@PathVariable String path,
                                                    @RequestParam boolean recursive,
                                                    @RequestParam String token,
                                                    @RequestParam boolean permissions);

    /**
     * Implementation Details:
     *
     * <p>The response from the server does not include a Content-Type header. To handle this, a custom decoder was created
     * to facilitate the decoding of the response.
     *
     * <p>For more information, refer to the custom decoder implementation in:
     * {@link MessageConversionCoreClientConfiguration#feignDecoder(com.fasterxml.jackson.databind.ObjectMapper)}
     */
    @GetMapping("/v1/conversations/{path}")
    ConversationDto getConversation(@PathVariable String path);

    @DeleteMapping("/v1/conversations/{path}")
    void deleteConversation(@PathVariable String path);
}
