package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.ApplicationResourceNodeInfoDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.dto.CreateApplicationResourceDto;
import com.epam.aidial.cfg.dto.ExportDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.ImportResourcesPreviewDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.ResourcePathDto;
import com.epam.aidial.cfg.dto.ResourcePathsDto;
import com.epam.aidial.cfg.mapper.ApplicationResourceMapper;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.service.ApplicationEximService;
import com.epam.aidial.cfg.service.ApplicationResourceService;
import com.epam.aidial.cfg.service.ZipApplicationEximService;
import com.epam.aidial.cfg.web.security.FullAdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/application-resources")
@Validated
@LogExecution
@RequiredArgsConstructor
public class ApplicationResourceController {

    private final ApplicationResourceService applicationService;
    private final ResourceMapper resourceMapper;
    private final ApplicationResourceMapper applicationResourceMapper;
    private final ApplicationEximService applicationEximService;
    private final ZipApplicationEximService zipApplicationEximService;

    @PostMapping(path = "/list",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ApplicationResourceNodeInfoDto getApplications(@RequestBody(required = false) ResourceMetadataRequestDto applicationsRequestDto) {
        var applicationsRequest = resourceMapper.toRequest(applicationsRequestDto);
        var applicationResource = applicationService.getApplications(applicationsRequest);
        return applicationResourceMapper.toApplicationResourceNodeInfoDto(applicationResource);
    }

    @PostMapping(path = "/get",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationResourceDto> getApplication(@RequestBody ResourcePathDto applicationPath,
                                                                 @RequestHeader(value = "If-None-Match") String etag) {
        var applicationResource = applicationService.getApplicationResource(applicationPath.getPath(), etag);
        var applicationResourceDto = applicationResourceMapper.toApplicationResourceDto(applicationResource.model());
        return ResponseEntity.status(HttpStatus.OK).eTag(applicationResource.etag()).body(applicationResourceDto);
    }

    @FullAdminOnly
    @PostMapping(path = "/create",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createApplication(@RequestBody CreateApplicationResourceDto createApplicationDto) {
        var createApplication = applicationResourceMapper.toCreateApplicationResourceDto(createApplicationDto);
        var currentEtag = applicationService.createApplicationResource(createApplication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(currentEtag).build();
    }

    @FullAdminOnly
    @PostMapping(
            path = "/update",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> updateApplication(
            @RequestHeader("If-Match") String etag,
            @RequestBody CreateApplicationResourceDto updateApplicationDto) {

        var updateApplication = applicationResourceMapper.toCreateApplicationResourceDto(updateApplicationDto);
        var currentEtag = applicationService.putApplicationResource(updateApplication, true,
                etag);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(currentEtag).build();
    }

    @FullAdminOnly
    @PostMapping(path = "/delete",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteApplicationResource(@RequestBody ResourcePathDto applicationPath,
                                          @RequestHeader(value = "If-Match") String etag) {
        applicationService.delete(applicationPath.getPath(), etag);
    }

    @FullAdminOnly
    @PostMapping(path = "/delete/bulk",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteApplicationResources(@RequestBody ResourcePathsDto applicationPaths) {
        var paths = applicationPaths.getPaths().stream().map(ResourcePathDto::getPath).toList();
        applicationService.deleteApplicationResources(paths);
    }

    @FullAdminOnly
    @PostMapping(path = "/move",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void moveApplicationResource(@RequestBody MoveResourceDto moveApplicationDto) {
        var moveApplication = resourceMapper.toMoveResource(moveApplicationDto);
        applicationService.move(moveApplication);
    }

    @PostMapping(path = "/export",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> exportApplicationsToZip(@RequestBody ExportDto exportApplicationsDto) {
        var stream = zipApplicationEximService.exportApplications(exportApplicationsDto.getPaths());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"applications_export.zip\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }

    @PostMapping(path = "/export/json",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ApplicationsEximDto exportApplicationsToJson(@RequestBody ExportDto exportApplicationsDto) {
        var applicationsExim = applicationEximService.exportApplications(exportApplicationsDto.getPaths());
        return applicationResourceMapper.toApplicationsEximDto(applicationsExim);
    }

    @FullAdminOnly
    @PostMapping(path = "/import/zip",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesFileResultDto importApplicationsFromZip(
            @RequestPart("config") @Validated ImportResourcesDto importApplicationsDto,
            @RequestPart("file") MultipartFile zipFile
    ) throws IOException {
        var importApplications = resourceMapper.toImportResources(importApplicationsDto);
        var importResult = zipApplicationEximService.importApplications(importApplications, zipFile);
        return resourceMapper.toImportResourcesFileResultDto(importResult);
    }

    @PostMapping(path = "/import/zip/preview",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesPreviewDto previewImportApplicationsFromZip(
            @RequestPart("config") @Validated ImportResourcesDto importApplicationsDto,
            @RequestPart("file") MultipartFile zipFile
    ) {
        var importApplications = resourceMapper.toImportResources(importApplicationsDto);
        var importResourcesPreview = zipApplicationEximService.previewImportApplicationsFromZip(importApplications, zipFile);
        return resourceMapper.toImportResourcesPreviewDto(importResourcesPreview);
    }

    @FullAdminOnly
    @PostMapping(path = "/import/json",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesFileResultDto importApplicationsFromJson(
            @RequestPart("config") @Validated ImportResourcesDto importApplicationsDto,
            @RequestPart("file") @Validated ApplicationsEximDto applicationsEximDto
    ) {
        var importApplications = resourceMapper.toImportResources(importApplicationsDto);
        var importResults = applicationEximService.importApplications(importApplications, applicationsEximDto);
        return resourceMapper.toImportResourcesFileResultDto(importResults);
    }

}