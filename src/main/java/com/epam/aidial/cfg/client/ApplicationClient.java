package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
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
        name = "applicationClient",
        url = "${core.client.url}",
        configuration = {
                MessageConversionCoreClientConfiguration.class,
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
                FeignErrorDecoderConfiguration.class
        })
public interface ApplicationClient {

    @GetMapping("/v1/metadata/applications/{path}")
    ApplicationMetadataDto getApplicationMetadata(@PathVariable String path,
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

    @GetMapping(value = "/v1/applications/{path}")
    ResponseEntity<ApplicationResourceDto> getApplicationResource(@PathVariable String path,
                                                                  @RequestHeader Map<String, String> headers);


    @PutMapping("/v1/applications/{path}")
    ResponseEntity<ApplicationMetadataDto> putApplicationResource(@PathVariable String path,
                                                  @RequestBody ApplicationResourceDto applicationResourceDto,
                                                  @RequestHeader Map<String, String> headers);

    @DeleteMapping("/v1/applications/{path}")
    void deleteApplicationResource(@PathVariable String path,
                                   @RequestHeader Map<String, String> headers);
}
