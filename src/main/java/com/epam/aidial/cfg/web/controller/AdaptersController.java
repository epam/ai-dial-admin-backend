package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.service.AdapterService;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/adapters")
@Validated
@LogExecution
public class AdaptersController {

    private final AdapterService adapterService;

    public AdaptersController(AdapterService adapterService) {
        this.adapterService = adapterService;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AdapterDto> getAll() {
        return adapterService.getAllAdapters();
    }

}
