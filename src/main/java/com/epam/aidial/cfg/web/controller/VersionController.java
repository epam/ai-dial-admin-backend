package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/version")
@LogExecution
public class VersionController {

    private final BuildProperties buildProperties;

    public VersionController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String getVersion() {
        return buildProperties.getVersion();
    }
}
