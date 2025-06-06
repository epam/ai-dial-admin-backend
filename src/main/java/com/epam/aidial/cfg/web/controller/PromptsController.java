package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.CreatePromptDto;
import com.epam.aidial.cfg.dto.ExportDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.PromptDto;
import com.epam.aidial.cfg.dto.PromptNodeInfoDto;
import com.epam.aidial.cfg.dto.PromptPathDto;
import com.epam.aidial.cfg.dto.PromptVersionsDto;
import com.epam.aidial.cfg.dto.PromptVersionsRequestDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.mapper.PromptMapper;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.service.prompt.PromptEximService;
import com.epam.aidial.cfg.service.prompt.PromptService;
import com.epam.aidial.cfg.service.prompt.ZipPromptEximService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/prompts")
@Validated
@LogExecution
@RequiredArgsConstructor
public class PromptsController {

    private final PromptService promptService;
    private final PromptEximService promptEximService;
    private final ZipPromptEximService zipPromptEximService;
    private final PromptMapper promptMapper;
    private final ResourceMapper resourceMapper;

    @PostMapping(path = "/list",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public PromptNodeInfoDto getPrompts(@RequestBody(required = false) ResourceMetadataRequestDto promptsRequestDto) {
        var promptsRequest = resourceMapper.toRequest(promptsRequestDto);
        var promptInfo = promptService.getPrompts(promptsRequest);
        return promptMapper.toPromptInfoDto(promptInfo);
    }

    @PostMapping(path = "/get",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public PromptDto getPrompt(@RequestBody PromptPathDto promptPath) {
        var prompt = promptService.getPrompt(promptPath.getPath());
        return promptMapper.toPromptDto(prompt);
    }

    @PostMapping(path = "/versions",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public PromptVersionsDto getPromptVersions(@RequestBody PromptVersionsRequestDto requestDto) {
        var promptInfos = promptService.getPromptVersions(requestDto.getFolderId(), requestDto.getName());
        return promptMapper.toPromptVersionsDto(promptInfos);
    }

    @PostMapping(path = "/create",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public PromptDto createPrompt(@RequestBody CreatePromptDto createPromptDto) {
        var createPrompt = promptMapper.toCreatePrompt(createPromptDto);
        var createdPrompt = promptService.createPrompt(createPrompt, true, null);
        return promptMapper.toPromptDto(createdPrompt);
    }

    @PostMapping(path = "/delete",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void deletePrompt(@RequestBody PromptPathDto promptPath) {
        promptService.deletePrompt(promptPath.getPath());
    }

    @PostMapping(path = "/move",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void movePrompt(@RequestBody MoveResourceDto movePromptDto) {
        var movePrompt = resourceMapper.toMoveResource(movePromptDto);
        promptService.movePrompt(movePrompt);
    }

    @PostMapping(path = "/export",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> exportPromptsToZip(@RequestBody ExportDto exportPromptsDto) {
        var stream = zipPromptEximService.exportPrompts(exportPromptsDto.getPaths());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"prompts_export.zip\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }

    @PostMapping(path = "/export/json",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public PromptsEximDto exportPromptsToJson(@RequestBody ExportDto exportPromptsDto) {
        var promptsExim = promptEximService.exportPrompts(exportPromptsDto.getPaths());
        return promptMapper.toPromptsEximDto(promptsExim);
    }

    @PostMapping(path = "/import/zip",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesFileResultDto importPromptsFromZip(
            @RequestPart("config") @Validated ImportResourcesDto importPromptsDto,
            @RequestPart("file") MultipartFile zipFile
    ) throws IOException {
        var importPrompts = resourceMapper.toImportResources(importPromptsDto);
        var importResult = zipPromptEximService.importPrompts(importPrompts, zipFile);
        return resourceMapper.toImportResourcesFileResultDto(importResult);
    }

    @PostMapping(path = "/import/json",
            consumes = "multipart/form-data",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ImportResourcesFileResultDto importPromptsFromJson(
            @RequestPart("config") @Validated ImportResourcesDto importPromptsDto,
            @RequestPart("file") @Validated PromptsEximDto promptsEximDto
    ) {
        var importPrompts = resourceMapper.toImportResources(importPromptsDto);
        var importResults = promptEximService.importPrompts(importPrompts, promptsEximDto);
        return resourceMapper.toImportResourcesFileResultDto(importResults);
    }

}
