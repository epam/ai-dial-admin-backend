package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.GlobalSettingsDto;
import com.epam.aidial.cfg.web.facade.GlobalSettingsFacade;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<GlobalSettingsDto> getGlobalSettings(@RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = globalSettingsFacade.getGlobalSettingsWithHash();
        return responseEntityForGet(dtoWithHash.dto(), dtoWithHash.hash(), previousHash);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateGlobalSettings(@RequestBody @Valid GlobalSettingsDto globalSettingsDto,
                                                     @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = globalSettingsFacade.updateGlobalSettings(globalSettingsDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public GlobalSettingsDto getAtRevision(@PathVariable Integer revision) {
        return globalSettingsFacade.getAtRevision(revision);
    }
}