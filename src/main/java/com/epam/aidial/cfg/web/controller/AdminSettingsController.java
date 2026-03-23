package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.AdminSettingsDto;
import com.epam.aidial.cfg.web.facade.AdminSettingsFacade;
import com.epam.aidial.cfg.web.security.FullAdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/admin-settings")
@Validated
@LogExecution
@RequiredArgsConstructor
public class AdminSettingsController extends AbstractController {

    private final AdminSettingsFacade adminSettingsFacade;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminSettingsDto> getAdminSettings(@RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = adminSettingsFacade.getAdminSettingsWithHash();
        return responseEntityForGet(dtoWithHash.dto(), dtoWithHash.hash(), previousHash);
    }

    @FullAdminOnly
    @PutMapping(path = "/core-config-version", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateCoreConfigVersion(@RequestBody @Valid AdminSettingsDto adminSettingsDto,
                                                        @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = adminSettingsFacade.updateCoreConfigVersion(adminSettingsDto.getCoreConfigVersion(), StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @GetMapping(path = "/revision/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminSettingsDto getAtRevision(@PathVariable Integer revision) {
        return adminSettingsFacade.getAtRevision(revision);
    }
}
