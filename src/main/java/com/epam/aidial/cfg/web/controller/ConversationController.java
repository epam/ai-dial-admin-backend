package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ConversationDto;
import com.epam.aidial.cfg.dto.ConversationNodeInfoDto;
import com.epam.aidial.cfg.dto.ConversationsEximDto;
import com.epam.aidial.cfg.dto.ExportDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.ImportResourcesPreviewDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.ResourcePathDto;
import com.epam.aidial.cfg.dto.ResourcePathsDto;
import com.epam.aidial.cfg.mapper.ConversationMapper;
import com.epam.aidial.cfg.mapper.PublicationMapper;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.service.ConversationEximService;
import com.epam.aidial.cfg.service.ConversationService;
import com.epam.aidial.cfg.service.ZipConversationEximService;
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
@RequestMapping("/api/v1/conversations")
@Validated
@LogExecution
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final ResourceMapper resourceMapper;
    private final ConversationMapper conversationMapper;
    private final PublicationMapper publicationMapper;
    private final ConversationEximService conversationEximService;
    private final ZipConversationEximService zipConversationEximService;

    @PostMapping(path = "/list",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ConversationNodeInfoDto getConversations(@RequestBody(required = false) ResourceMetadataRequestDto requestDto) {
        var request = resourceMapper.toRequest(requestDto);
        var nodeInfo = conversationService.getConversations(request);
        return conversationMapper.toConversationNodeInfoDto(nodeInfo);
    }

    @PostMapping(path = "/get",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConversationDto> getConversation(@RequestBody ResourcePathDto pathDto,
                                                           @RequestHeader(value = "If-None-Match") String etag) {
        var withEtag = conversationService.getConversation(pathDto.getPath(), etag);
        var dto = publicationMapper.toConversationDto(withEtag.model());
        return ResponseEntity.status(HttpStatus.OK).eTag(withEtag.etag()).body(dto);
    }

    @FullAdminOnly
    @PostMapping(path = "/delete",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteConversation(@RequestBody ResourcePathDto pathDto,
                                   @RequestHeader(value = "If-Match") String etag) {
        conversationService.delete(pathDto.getPath(), etag);
    }

    @FullAdminOnly
    @PostMapping(path = "/delete/bulk",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deleteConversations(@RequestBody ResourcePathsDto pathsDto) {
        var paths = pathsDto.getPaths().stream().map(ResourcePathDto::getPath).toList();
        conversationService.deleteConversations(paths);
    }

    @FullAdminOnly
    @PostMapping(path = "/move",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void moveConversation(@RequestBody MoveResourceDto moveDto) {
        var move = resourceMapper.toMoveResource(moveDto);
        conversationService.move(move);
    }

    @PostMapping(path = "/export",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> exportConversationsToZip(@RequestBody ExportDto exportDto) {
        var stream = zipConversationEximService.exportConversations(exportDto.getPaths());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"conversations_export.zip\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }

    @PostMapping(path = "/export/json",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ConversationsEximDto exportConversationsToJson(@RequestBody ExportDto exportDto) {
        var conversationsExim = conversationEximService.exportConversations(exportDto.getPaths());
        return conversationMapper.toConversationsEximDto(conversationsExim);
    }

    @FullAdminOnly
    @PostMapping(path = "/import/zip",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesFileResultDto importConversationsFromZip(
            @RequestPart("config") @Validated ImportResourcesDto importDto,
            @RequestPart("file") MultipartFile zipFile
    ) throws IOException {
        var importResources = resourceMapper.toImportResources(importDto);
        var importResult = zipConversationEximService.importConversations(importResources, zipFile);
        return resourceMapper.toImportResourcesFileResultDto(importResult);
    }

    @PostMapping(path = "/import/zip/preview",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesPreviewDto previewImportConversationsFromZip(
            @RequestPart("config") @Validated ImportResourcesDto importDto,
            @RequestPart("file") MultipartFile zipFile
    ) {
        var importResources = resourceMapper.toImportResources(importDto);
        var preview = zipConversationEximService.previewImportConversationsFromZip(importResources, zipFile);
        return resourceMapper.toImportResourcesPreviewDto(preview);
    }

    @FullAdminOnly
    @PostMapping(path = "/import/json",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesFileResultDto importConversationsFromJson(
            @RequestPart("config") @Validated ImportResourcesDto importDto,
            @RequestPart("file") @Validated ConversationsEximDto conversationsEximDto
    ) {
        var importResources = resourceMapper.toImportResources(importDto);
        var importResult = conversationEximService.importConversations(importResources, conversationsEximDto);
        return resourceMapper.toImportResourcesFileResultDto(importResult);
    }
}