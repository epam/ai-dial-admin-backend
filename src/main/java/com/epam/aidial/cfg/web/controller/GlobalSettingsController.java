package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.GlobalInterceptorsDto;
import com.epam.aidial.cfg.web.facade.GlobalSettingsFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/global-settings")
@Validated
@LogExecution
public class GlobalSettingsController extends AbstractController {

    private final GlobalSettingsFacade globalSettingsFacade;

    public GlobalSettingsController(GlobalSettingsFacade globalSettingsFacade) {
        this.globalSettingsFacade = globalSettingsFacade;
    }

    @GetMapping(path = "/global-interceptors",
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<String> getAllGlobalInterceptors() {
        return globalSettingsFacade.getAllGlobalInterceptors();
    }

    @PostMapping(path = "/global-interceptors",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveGlobalInterceptors(@RequestBody @Valid GlobalInterceptorsDto globalInterceptorsDto) {
        if (CollectionUtils.isEmpty(globalInterceptorsDto.getGlobalInterceptorIds())) {
            throw new IllegalArgumentException("List ids must not be null or empty");
        }
        globalSettingsFacade.saveGlobalInterceptors(globalInterceptorsDto.getGlobalInterceptorIds());
    }

    @GetMapping(path = "/global-interceptors/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<String> getAllAtRevision(@PathVariable Integer revision) {
        return globalSettingsFacade.getAllAtRevision(revision);
    }
}