package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.CoreConfigVersionsDto;
import com.epam.aidial.cfg.model.CoreConfigVersions;
import com.epam.aidial.cfg.service.config.transfer.version.CoreConfigVersionService;
import com.epam.aidial.cfg.web.facade.mapper.CoreConfigVersionsDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/version")
@LogExecution
@RequiredArgsConstructor
public class VersionController {

    private final BuildProperties buildProperties;
    private final CoreConfigVersionService coreConfigVersionService;
    private final CoreConfigVersionsDtoMapper coreConfigVersionsDtoMapper;

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)

    public String getVersion() {
        return buildProperties.getVersion();
    }

    @GetMapping(path = "/core", produces = MediaType.TEXT_PLAIN_VALUE)
    public CoreConfigVersionsDto getCoreVersions() {
        CoreConfigVersions versions = coreConfigVersionService.getVersions();
        return coreConfigVersionsDtoMapper.toDto(versions);
    }
}
