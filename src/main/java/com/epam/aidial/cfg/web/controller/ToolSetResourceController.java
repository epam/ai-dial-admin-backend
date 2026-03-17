package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.CallToolResourceRequestDto;
import com.epam.aidial.cfg.dto.CreateToolSetResourceDto;
import com.epam.aidial.cfg.dto.ExportDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.ImportResourcesPreviewDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.ResourcePathDto;
import com.epam.aidial.cfg.dto.ResourcePathsDto;
import com.epam.aidial.cfg.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.dto.ResourceSignOutRequestDto;
import com.epam.aidial.cfg.dto.ToolSetResourceDto;
import com.epam.aidial.cfg.dto.ToolSetResourceNodeInfoDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.mapper.ResourceCredentialMapper;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.mapper.ToolSetResourceMapper;
import com.epam.aidial.cfg.service.ResourceCredentialService;
import com.epam.aidial.cfg.service.ToolSetEximService;
import com.epam.aidial.cfg.service.ToolSetResourceService;
import com.epam.aidial.cfg.service.ZipToolSetEximService;
import com.epam.aidial.cfg.web.security.FullAdminOnly;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/toolset-resources")
@Validated
@LogExecution
@RequiredArgsConstructor
public class ToolSetResourceController {

    private final ToolSetResourceService toolSetResourceService;
    private final ResourceCredentialService resourceCredentialService;
    private final ResourceMapper resourceMapper;
    private final ToolSetResourceMapper toolSetResourceMapper;
    private final ResourceCredentialMapper resourceCredentialMapper;
    private final ToolSetEximService toolSetEximService;
    private final ZipToolSetEximService zipToolSetEximService;

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

    @FullAdminOnly
    @PostMapping(path = "/create",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createToolSetResource(@RequestBody CreateToolSetResourceDto createToolSetResourceDto) {
        var createToolSet = toolSetResourceMapper.toCreateToolSetResourceDto(createToolSetResourceDto);
        var currentEtag = toolSetResourceService.createToolSetResource(createToolSet);
        return ResponseEntity.noContent().eTag(currentEtag).build();
    }

    @FullAdminOnly
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


    @FullAdminOnly
    @PostMapping(path = "/delete",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteToolSetResource(@RequestBody ResourcePathDto resourcePath,
                                      @RequestHeader(value = "If-Match") String etag) {
        toolSetResourceService.delete(resourcePath.getPath(), etag);
    }

    @FullAdminOnly
    @PostMapping(path = "/delete/bulk",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteToolSetResources(@RequestBody ResourcePathsDto toolSetPaths) {
        var paths = toolSetPaths.getPaths().stream().map(ResourcePathDto::getPath).toList();
        toolSetResourceService.deleteToolSetResources(paths);
    }

    @FullAdminOnly
    @PostMapping(path = "/move",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void moveToolSetResource(@RequestBody MoveResourceDto movePromptDto) {
        var moveToolSet = resourceMapper.toMoveResource(movePromptDto);
        toolSetResourceService.move(moveToolSet);
    }

    @PostMapping(path = "/discovered-tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public McpSchema.ListToolsResult getDiscoveredTools(@RequestBody ResourcePathDto toolSetPath,
                                                        @RequestParam(required = false) String nextCursor) {
        return toolSetResourceService.getDiscoveredTools(toolSetPath.getPath(), nextCursor);
    }

    @PostMapping(path = "/call-tool", produces = MediaType.APPLICATION_JSON_VALUE)
    public McpSchema.CallToolResult callTool(@RequestBody CallToolResourceRequestDto callToolResourceRequestDto) {
        return toolSetResourceService.callTool(
                callToolResourceRequestDto.getToolSetPath().getPath(),
                callToolResourceRequestDto.getCallToolRequest()
        );
    }

    @PostMapping(path = "/sign-in")
    public void signIn(@RequestBody ResourceSignInRequestDto requestDto) {
        resourceCredentialService.signInToolSet(resourceCredentialMapper.toResourceSignInRequest(requestDto));
    }

    @PostMapping(path = "/sign-out")
    public void signOut(@RequestBody ResourceSignOutRequestDto requestDto) {
        resourceCredentialService.signOutToolSet(resourceCredentialMapper.toResourceSignOutRequest(requestDto));
    }

    @PostMapping(path = "/export",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> exportToolSetsToZip(@RequestBody ExportDto exportToolSetsDto) {
        var stream = zipToolSetEximService.exportToolSets(exportToolSetsDto.getPaths());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"toolsets_export.zip\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }

    @PostMapping(path = "/export/json",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ToolSetsEximDto exportToolSetsToJson(@RequestBody ExportDto exportToolSetsDto) {
        var toolSetsExim = toolSetEximService.exportToolSets(exportToolSetsDto.getPaths());
        return toolSetResourceMapper.toToolSetsEximDto(toolSetsExim);
    }

    @FullAdminOnly
    @PostMapping(path = "/import/zip",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesFileResultDto importToolSetsFromZip(
            @RequestPart("config") @Validated ImportResourcesDto importToolSetsDto,
            @RequestPart("file") MultipartFile zipFile
    ) throws IOException {
        var importToolSets = resourceMapper.toImportResources(importToolSetsDto);
        var importResult = zipToolSetEximService.importToolSets(importToolSets, zipFile);
        return resourceMapper.toImportResourcesFileResultDto(importResult);
    }

    @PostMapping(path = "/import/zip/preview",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesPreviewDto previewImportToolSetsFromZip(
            @RequestPart("config") @Validated ImportResourcesDto importToolSetsDto,
            @RequestPart("file") MultipartFile zipFile
    ) {
        var importToolSets = resourceMapper.toImportResources(importToolSetsDto);
        var importResourcesPreview = zipToolSetEximService.previewImportToolSetsFromZip(importToolSets, zipFile);
        return resourceMapper.toImportResourcesPreviewDto(importResourcesPreview);
    }

    @FullAdminOnly
    @PostMapping(path = "/import/json",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesFileResultDto importToolSetsFromJson(
            @RequestPart("config") @Validated ImportResourcesDto importToolSetsDto,
            @RequestPart("file") @Validated ToolSetsEximDto toolSetsEximDto
    ) {
        var importToolSets = resourceMapper.toImportResources(importToolSetsDto);
        var importResults = toolSetEximService.importToolSets(importToolSets, toolSetsEximDto);
        return resourceMapper.toImportResourcesFileResultDto(importResults);
    }

}