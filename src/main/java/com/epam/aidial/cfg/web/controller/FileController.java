package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.dto.ExportDto;
import com.epam.aidial.cfg.dto.FileNodeInfoDto;
import com.epam.aidial.cfg.dto.FilePathsDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.validation.annotation.MetadataPath;
import com.epam.aidial.cfg.web.facade.FileResourceFacade;
import feign.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@Validated
@RequiredArgsConstructor
public class FileController {

    private final FileResourceFacade facade;

    @PostMapping()
    public FileNodeInfoDto getAll(@RequestBody(required = false) ResourceMetadataRequestDto requestDto) {
        return facade.getAll(requestDto);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("path") String path) throws IOException {
        Response response = facade.getByPath(path);
        int status = response.status();
        if (status == 200) {
            String fileName = extractFileName(path);
            HttpHeaders httpHeaders = new HttpHeaders();
            String contentType = extractContentType(response);
            if (StringUtils.isNotEmpty(contentType)) {
                httpHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
            }
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + fileName + "\"");
            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .body(response.body().asInputStream().readAllBytes());
        }
        return ResponseEntity.status(HttpStatusCode.valueOf(status)).build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResourcesFileResultDto uploadFiles(@RequestPart("files") @Valid @Size(min = 1, max = 30) List<MultipartFile> files,
                                                    @RequestPart("config") @Validated ImportResourcesDto importFilesDto) {
        return facade.uploadFiles(files, importFilesDto);
    }

    @PostMapping(value = "/import/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResourcesFileResultDto uploadFilesZip(@RequestPart("file") MultipartFile files,
                                                       @RequestPart("config") @Validated ImportResourcesDto importFilesDto) {
        return facade.uploadFilesZip(files, importFilesDto);
    }

    @DeleteMapping()
    public void deleteFile(@RequestParam("path") @MetadataPath String path) {
        facade.deleteFile(path);
    }

    @PostMapping(value = "/delete/bulk")
    public void deleteFiles(@RequestBody FilePathsDto filePaths) {
        facade.deleteFiles(filePaths);
    }

    @PostMapping(path = "/move",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void moveFile(@RequestBody MoveResourceDto moveResourceDto) {
        facade.moveFile(moveResourceDto);
    }

    @PostMapping(path = "/export",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> export(@RequestBody ExportDto exportDto) {
        var stream = facade.export(exportDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"files_export.zip\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }

    private String extractContentType(Response response) {
        Collection<String> contentTypes = response.headers().get("Content-Type");

        if (CollectionUtils.isNotEmpty(contentTypes)) {
            String firstContentType = contentTypes.iterator().next();
            return MediaType.parseMediaType(firstContentType).toString();
        }
        return null;
    }

    private static String extractFileName(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("The file path does not contain a '/': %s".formatted(path));
        }
        return path.substring(lastSlashIndex + 1);
    }
}