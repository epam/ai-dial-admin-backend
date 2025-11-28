package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.GlobalSettingsDto;
import com.epam.aidial.cfg.web.facade.GlobalSettingsFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/global-settings")
@Validated
@LogExecution
public class GlobalSettingsController extends AbstractController {

    private final GlobalSettingsFacade globalSettingsFacade;

    public GlobalSettingsController(GlobalSettingsFacade globalSettingsFacade) {
        this.globalSettingsFacade = globalSettingsFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public GlobalSettingsDto getGlobalSettings() {
        return globalSettingsFacade.getGlobalSettings();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveGlobalSettings(@RequestBody @Valid GlobalSettingsDto globalSettingsDto) {
        globalSettingsFacade.saveGlobalSettings(globalSettingsDto);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public GlobalSettingsDto getAtRevision(@PathVariable Integer revision) {
        return globalSettingsFacade.getAtRevision(revision);
    }
}