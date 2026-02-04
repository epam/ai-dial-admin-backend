package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.ToolSetMetadataDto;
import com.epam.aidial.cfg.client.dto.ToolSetResourceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "toolServiceClient",
        url = "${core.client.url}",
        configuration = {
                MessageConversionCoreClientConfiguration.class,
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        })
public interface ToolSetClient {

    @GetMapping("/v1/metadata/toolsets/{path}")
    ToolSetMetadataDto getToolSetMetadata(@PathVariable String path,
                                          @RequestParam boolean recursive,
                                          @RequestParam String token,
                                          @RequestParam int limit,
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

    @GetMapping(value = "/v1/toolsets/{path}")
    ResponseEntity<ToolSetResourceDto> getToolSetResource(@PathVariable String path,
                                                          @RequestHeader Map<String, String> headers);

    @PutMapping("/v1/toolsets/{path}")
    ResponseEntity<ToolSetMetadataDto> putToolSetResource(@PathVariable String path,
                                                          @RequestBody ToolSetResourceDto toolSetResourceDto,
                                                          @RequestHeader Map<String, String> headers);

    @DeleteMapping("/v1/toolsets/{path}")
    void deleteToolSetResource(@PathVariable String path,
                               @RequestHeader Map<String, String> headers);

}