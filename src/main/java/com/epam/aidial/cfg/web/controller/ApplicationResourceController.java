package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.ApplicationResourceNodeInfoDto;
import com.epam.aidial.cfg.dto.CreateApplicationResourceDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.ResourcePathDto;
import com.epam.aidial.cfg.dto.ResourcePathsDto;
import com.epam.aidial.cfg.mapper.ApplicationResourceMapper;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.service.ApplicationResourceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/application-resources")
@Validated
@LogExecution
@RequiredArgsConstructor
public class ApplicationResourceController {

    private final ApplicationResourceService applicationService;
    private final ResourceMapper resourceMapper;
    private final ApplicationResourceMapper applicationResourceMapper;

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
                                                                 @RequestHeader(value = "If-None-Match") String etag) throws JsonProcessingException {
        var applicationResource = applicationService.getApplicationResource(applicationPath.getPath(), etag);
        var applicationResourceDto = applicationResourceMapper.toApplicationResourceDto(applicationResource.model());
        return ResponseEntity.status(HttpStatus.OK).eTag(applicationResource.etag()).body(applicationResourceDto);
    }

    @PostMapping(path = "/create",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationResourceDto> createApplication(@RequestBody CreateApplicationResourceDto createApplicationDto) throws JsonProcessingException {
        var createApplication = applicationResourceMapper.toCreateApplicationResourceDto(createApplicationDto);
        var createdApplication = applicationService.putApplicationResource(createApplication, false, null);
        return ResponseEntity.ok()
                .eTag(createdApplication.etag())
                .body(applicationResourceMapper.toApplicationResourceDto(createdApplication.model()));
    }

    @PostMapping(
            path = "/update",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApplicationResourceDto> updateApplication(
            @RequestHeader("If-Match") String etag,
            @RequestBody CreateApplicationResourceDto updateApplicationDto) {

        var updateApplication = applicationResourceMapper.toCreateApplicationResourceDto(updateApplicationDto);
        var updatedApplication = applicationService.putApplicationResource(updateApplication, true,
                etag);
        return ResponseEntity.ok()
                .eTag(updatedApplication.etag())
                .body(applicationResourceMapper.toApplicationResourceDto(updatedApplication.model()));
    }


    @PostMapping(path = "/delete",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteApplicationResource(@RequestBody ResourcePathDto applicationPath,
                                          @RequestHeader(value = "If-Match") String etag) {
        applicationService.deleteApplicationResource(applicationPath.getPath(), etag);
    }

    @PostMapping(path = "/delete/bulk",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteApplicationResources(@RequestBody ResourcePathsDto applicationPaths) {
        var paths = applicationPaths.getPaths().stream().map(ResourcePathDto::getPath).toList();
        applicationService.deleteApplicationResources(paths);
    }

    @PostMapping(path = "/move",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void moveApplicationResource(@RequestBody MoveResourceDto movePromptDto) {
        var movePrompt = resourceMapper.toMoveResource(movePromptDto);
        applicationService.move(movePrompt);
    }

}
