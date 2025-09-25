package com.epam.aidial.cfg.client;


import com.epam.aidial.cfg.client.dto.FileMetadataDto;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(name = "fileClient",
        url = "${core.client.url}",
        configuration = {
                AuthorizationCoreClientConfiguration.class,
                RetryClientConfiguration.class,
                FeignErrorDecoderConfiguration.class})
public interface FileClient {

    @GetMapping("/v1/metadata/files/{path}")
    FileMetadataDto getFilesMetadata(@PathVariable String path,
                                     @RequestParam boolean recursive,
                                     @RequestParam String token);

    @GetMapping("/v1/files/{path}")
    Response getFile(@PathVariable String path);

    @DeleteMapping("/v1/files/{path}")
    void deleteFile(@PathVariable String path);

    @PutMapping(value = "/v1/files/{path}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void uploadFile(@RequestPart("file") MultipartFile file,
                    @PathVariable("path") String path,
                    @RequestHeader Map<String, String> headers);

}
