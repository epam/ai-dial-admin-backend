package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.web.facade.AddonFacade;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/addons")
@Validated
@LogExecution
public class AddonController {

    private final AddonFacade addonFacade;

    public AddonController(AddonFacade addonFacade) {
        this.addonFacade = addonFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AddonDto> getAll(HttpServletResponse response) {
        return addonFacade.getAllAddons();
    }

    @GetMapping(path = "/{addonName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AddonDto getAddon(HttpServletResponse response,
                             @PathVariable("addonName") String addonName) {
        return addonFacade.getAddon(addonName);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createAddon(HttpServletResponse response,
                            @RequestBody @Valid AddonDto addonDto) {
        addonFacade.createAddon(addonDto);
    }

    @DeleteMapping(path = "/{addonName}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddon(HttpServletResponse response,
                            @PathVariable("addonName") String addonName) {
        addonFacade.deleteAddon(addonName);
    }

    @PutMapping(path = "/{addonName}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAddon(HttpServletResponse response,
                            @PathVariable("addonName") String addonName,
                            @RequestBody @Valid AddonDto addonDto) {
        addonFacade.updateAddon(addonName, addonDto);
    }

    @GetMapping(path = "/{addonName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AddonDto getSnapshot(@PathVariable String addonName, @PathVariable Integer revision) {
        return addonFacade.getSnapshot(addonName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AddonDto> getAllAtRevision(HttpServletResponse response, @PathVariable Integer revision) throws Exception {
        return addonFacade.getAllAtRevision(revision);
    }
}
