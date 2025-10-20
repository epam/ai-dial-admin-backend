package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.CreateToolSetResourceDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.ResourcePathDto;
import com.epam.aidial.cfg.dto.ResourcePathsDto;
import com.epam.aidial.cfg.dto.ToolSetResourceDto;
import com.epam.aidial.cfg.dto.ToolSetResourceNodeInfoDto;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.mapper.ToolSetResourceMapper;
import com.epam.aidial.cfg.service.ToolSetResourceService;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/toolset-resources")
@Validated
@LogExecution
@RequiredArgsConstructor
public class ToolSetResourceController {

    private final ToolSetResourceService toolSetResourceService;
    private final ResourceMapper resourceMapper;
    private final ToolSetResourceMapper toolSetResourceMapper;

    @PostMapping(path = "/list",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ToolSetResourceNodeInfoDto getToolSets(@RequestBody(required = false) ResourceMetadataRequestDto requestDto) {
        var resourcesRequest = resourceMapper.toRequest(requestDto);
        var toolSetResources = toolSetResourceService.getToolSetResources(resourcesRequest);
        return toolSetResourceMapper.toToolSetResourceNodeInfoDto(toolSetResources);
    }

    @PostMapping(path = "/get",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolSetResourceDto> getToolSetResource(@RequestBody ResourcePathDto toolSetPath,
                                                                 @RequestHeader(value = "If-None-Match") String etag) {
        var toolSetResource = toolSetResourceService.getToolSetResource(toolSetPath.getPath(), etag);
        var toolSetResourceDto = toolSetResourceMapper.toToolSetResourceDto(toolSetResource.model());
        return ResponseEntity.status(HttpStatus.OK).eTag(toolSetResource.etag()).body(toolSetResourceDto);
    }

    @PostMapping(path = "/create",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createToolSetResource(@RequestBody CreateToolSetResourceDto createToolSetResourceDto) {
        var createToolSet = toolSetResourceMapper.toCreateToolSetResourceDto(createToolSetResourceDto);
        var currentEtag = toolSetResourceService.createToolSetResource(createToolSet);
        return ResponseEntity.noContent().eTag(currentEtag).build();
    }

    @PostMapping(
            path = "/update",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateToolSetResource(
            @RequestHeader("If-Match") String etag,
            @RequestBody CreateToolSetResourceDto updateToolSetDto) {

        var updateToolSet = toolSetResourceMapper.toCreateToolSetResourceDto(updateToolSetDto);
        var currentEtag = toolSetResourceService.putToolSetResource(updateToolSet, true,
                etag);
        return ResponseEntity.noContent().eTag(currentEtag).build();
    }


    @PostMapping(path = "/delete",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteToolSetResource(@RequestBody ResourcePathDto resourcePath,
                                      @RequestHeader(value = "If-Match") String etag) {
        toolSetResourceService.deleteToolSetResource(resourcePath.getPath(), etag);
    }

    @PostMapping(path = "/delete/bulk",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteToolSetResources(@RequestBody ResourcePathsDto toolSetPaths) {
        var paths = toolSetPaths.getPaths().stream().map(ResourcePathDto::getPath).toList();
        toolSetResourceService.deleteToolSetResources(paths);
    }

    @PostMapping(path = "/move",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void moveToolSetResource(@RequestBody MoveResourceDto movePromptDto) {
        var movePrompt = resourceMapper.toMoveResource(movePromptDto);
        toolSetResourceService.move(movePrompt);
    }

    @PostMapping(path = "/discovered-tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public McpSchema.ListToolsResult getDiscoveredTools(@RequestBody ResourcePathDto toolSetPath,
                                                        @RequestParam(required = false) String nextCursor) {
        return toolSetResourceService.getDiscoveredTools(toolSetPath.getPath(), nextCursor);
    }

}
