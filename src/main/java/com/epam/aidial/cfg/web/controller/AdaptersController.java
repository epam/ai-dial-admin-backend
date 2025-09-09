package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/adapters")
@Validated
@LogExecution
@RequiredArgsConstructor
public class AdaptersController {

    private final AdapterFacade adapterFacade;

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AdapterDto> getAll() {
        return adapterFacade.getAllAdapters();
    }

    @GetMapping(path = "/{adapterName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AdapterDto getAdapter(@PathVariable String adapterName) {
        return adapterFacade.getAdapter(adapterName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createAdapter(@RequestBody @Valid AdapterDto adapterDto) {
        adapterFacade.createAdapter(adapterDto);
    }

    @DeleteMapping(path = "/{adapterName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdapter(@PathVariable String adapterName,
                              @RequestParam(name = "removeModel", required = false, defaultValue = "true") boolean removeModel) {
        adapterFacade.deleteAdapter(adapterName, removeModel);
    }

    @PutMapping(path = "/{adapterName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAdapter(@PathVariable String adapterName,
                              @RequestBody @Valid AdapterDto adapterDto) {
        adapterFacade.updateAdapter(adapterName, adapterDto);
    }

    @GetMapping(path = "/{adapterName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AdapterDto getSnapshot(@PathVariable String adapterName, @PathVariable Integer revision) {
        return adapterFacade.getSnapshot(adapterName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AdapterDto> getAllAtRevision(@PathVariable Integer revision) {
        return adapterFacade.getAllAtRevision(revision);
    }
}
