package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.web.facade.AddonFacade;
import com.epam.aidial.cfg.web.security.FullAdminOnly;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/addons")
@Validated
@LogExecution
public class AddonController extends AbstractController {

    private final AddonFacade addonFacade;

    public AddonController(AddonFacade addonFacade) {
        this.addonFacade = addonFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AddonDto> getAll() {
        return addonFacade.getAllAddons();
    }

    @GetMapping(path = "/{addonName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddonDto> getAddon(@PathVariable("addonName") String addonName,
                                             @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = addonFacade.getAddonWithHash(addonName);
        return responseEntityForGet(dtoWithHash.dto(), dtoWithHash.hash(), previousHash);
    }

    @FullAdminOnly
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createAddon(@RequestBody @Valid AddonDto addonDto) {
        addonFacade.createAddon(addonDto);
    }

    @FullAdminOnly
    @DeleteMapping(path = "/{addonName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddon(@PathVariable("addonName") String addonName) {
        addonFacade.deleteAddon(addonName);
    }

    @FullAdminOnly
    @PutMapping(path = "/{addonName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateAddon(@PathVariable("addonName") String addonName,
                                            @RequestBody @Valid AddonDto addonDto,
                                            @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = addonFacade.updateAddon(addonName, addonDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @GetMapping(path = "/{addonName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AddonDto getSnapshot(@PathVariable String addonName, @PathVariable Integer revision) {
        return addonFacade.getSnapshot(addonName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AddonDto> getAllAtRevision(@PathVariable Integer revision) {
        return addonFacade.getAllAtRevision(revision);
    }
}
