package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.SecurityInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/security-info")
@LogExecution
@RequiredArgsConstructor
public class SecurityInfoController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public SecurityInfoDto getSecurityInfo() {
        return new SecurityInfoDto();
    }
}
