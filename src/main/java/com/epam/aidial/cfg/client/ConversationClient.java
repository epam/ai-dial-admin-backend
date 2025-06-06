package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "conversationClient", url = "${core.client.url}", configuration = AuthorizationCoreClientConfiguration.class)
public interface ConversationClient {

    @GetMapping("/v1/metadata/conversations/{path}")
    ConversationMetadataDto getConversationMetadata(@PathVariable String path,
                                                    @RequestParam boolean recursive,
                                                    @RequestParam String token);
}
