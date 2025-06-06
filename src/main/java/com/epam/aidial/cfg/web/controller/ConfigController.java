package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.ConfigExportProperties;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ExportConfigMapper;
import com.epam.aidial.cfg.dto.ConfigExportDto;
import com.epam.aidial.cfg.dto.ExportConfigPreviewDto;
import com.epam.aidial.cfg.dto.ExportRequestDto;
import com.epam.aidial.cfg.dto.ImportConfigPreviewDto;
import com.epam.aidial.cfg.service.export.ConfigExportErrorHandler;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.export.CoreConfigService;
import com.epam.aidial.cfg.service.transfer.ConfigTransfer;
import com.epam.aidial.cfg.web.facade.mapper.ImportConfigMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configs")
@Validated
@LogExecution
public class ConfigController {

    private final CoreConfigService coreConfigService;
    private final ConfigTransfer configTransfer;
    private final ExportConfigMapper exportConfigMapper;
    private final ImportConfigMapper importConfigMapper;
    private final ConfigExportProperties properties;
    private final int importConfigsMaxCount;
    private final ConfigExportErrorHandler configExportErrorHandler;

    public ConfigController(CoreConfigService coreConfigService,
                            ConfigTransfer configTransfer,
                            ExportConfigMapper exportConfigMapper,
                            ImportConfigMapper importConfigMapper,
                            ConfigExportProperties properties,
                            @Value("${config.import.configsMaxCount}") int importConfigsMaxCount,
                            ConfigExportErrorHandler configExportErrorHandler) {
        this.coreConfigService = coreConfigService;
        this.configTransfer = configTransfer;
        this.exportConfigMapper = exportConfigMapper;
        this.importConfigMapper = importConfigMapper;
        this.properties = properties;
        this.importConfigsMaxCount = importConfigsMaxCount;
        this.configExportErrorHandler = configExportErrorHandler;
    }

    @GetMapping(path = "/reload")
    public void reload() throws Exception {
        coreConfigService.reloadConfig();
    }

    @GetMapping(path = "/export/status")
    public ConfigExportDto getConfigExportStatus() {
        String errorMessage = configExportErrorHandler.getLastErrorMessage();
        boolean isSuccess = !StringUtils.isNotEmpty(errorMessage);
        return new ConfigExportDto(isSuccess, errorMessage);
    }

    @PostMapping(path = "/export", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> exportConfig(@Valid @RequestBody ExportRequestDto dto) {
        var request = exportConfigMapper.toExportRequest(dto);
        var stream = configTransfer.exportConfig(request);

        HttpHeaders headers = new HttpHeaders();
        var headerValue = switch (request.getExportFormat()) {
            case CORE -> "attachment; filename=\"" + properties.getExportConfigFileName() + "\"";
            case ADMIN -> "attachment; filename=\"" + properties.getExportConfigFileZipName() + "\"";
        };
        headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        headers.add("Content-Disposition", headerValue);
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }

    @PostMapping(path = "/export/preview")
    public ExportConfigPreviewDto preview(@Valid @RequestBody ExportRequestDto dto) {
        var request = exportConfigMapper.toExportRequest(dto);
        var preview = configTransfer.exportPreview(request);
        return exportConfigMapper.toExportConfigPreviewDto(preview);
    }

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importConfig(@RequestPart("file") @Valid @Size(min = 1) List<MultipartFile> files,
                             @RequestParam("resolutionPolicy") ConflictResolutionPolicy resolutionPolicy,
                             @RequestParam(value = "createRoleIfAbsent", required = false, defaultValue = "true") boolean createRoleIfAbsent) {
        int filesSize = CollectionUtils.size(files);
        if (filesSize > importConfigsMaxCount) {
            throw new IllegalArgumentException(String.format("Exceeded maximum file upload limit. Can upload up to %d files, but found %d.",
                    importConfigsMaxCount, filesSize));
        }
        configTransfer.importConfig(files, resolutionPolicy, createRoleIfAbsent);
    }

    @PostMapping(path = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportConfigPreviewDto importConfigPreview(@RequestPart("file") @Valid @Size(min = 1) List<MultipartFile> files,
                                                      @RequestParam("resolutionPolicy") ConflictResolutionPolicy resolutionPolicy) {
        int filesSize = CollectionUtils.size(files);
        if (filesSize > importConfigsMaxCount) {
            throw new IllegalArgumentException(String.format("Exceeded maximum file upload limit. Can upload up to %d files, but found %d.",
                    importConfigsMaxCount, filesSize));
        }
        var importConfigPreview = configTransfer.importPreview(files, resolutionPolicy);
        return importConfigMapper.toImportConfigPreviewDto(importConfigPreview);
    }

    @PostMapping(path = "/import/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importConfigZip(@RequestPart("file") MultipartFile file,
                                @RequestParam("resolutionPolicy") ConflictResolutionPolicy resolutionPolicy,
                                @RequestParam(value = "createRoleIfAbsent", required = false, defaultValue = "true") boolean createRoleIfAbsent) {
        configTransfer.importConfigZip(file, resolutionPolicy, createRoleIfAbsent);
    }

    @PostMapping(path = "/import/zip/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportConfigPreviewDto importConfigZipPreview(@RequestPart("file") MultipartFile file,
                                                         @RequestParam("resolutionPolicy") ConflictResolutionPolicy resolutionPolicy) {
        var importConfigPreview = configTransfer.importPreviewZip(file, resolutionPolicy);
        return importConfigMapper.toImportConfigPreviewDto(importConfigPreview);
    }

}
