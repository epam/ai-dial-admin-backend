package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
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
@RequestMapping("/api/v1/applications")
@Validated
@LogExecution
public class ApplicationController {

    private final ApplicationFacade applicationFacade;

    public ApplicationController(ApplicationFacade applicationFacade) {
        this.applicationFacade = applicationFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ApplicationInfoDto> getAllApplications() {
        return applicationFacade.getAllApplications();
    }

    @GetMapping(path = "/{applicationName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationDto> getApplication(@PathVariable("applicationName") String applicationName,
                                                         @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = applicationFacade.getApplicationWithHash(applicationName);
        return dtoWithHash.hash().equals(StringUtils.unwrap(previousHash, '"'))
                ? ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(dtoWithHash.hash()).build()
                : ResponseEntity.status(HttpStatus.OK).eTag(dtoWithHash.hash()).body(dtoWithHash.dto());

    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createApplication(@RequestBody @Valid ApplicationDto applicationDto) {
        applicationFacade.createApplication(applicationDto);
    }

    @PutMapping(path = "/{applicationName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateApplication(@PathVariable("applicationName") String applicationName,
                                                  @RequestBody @Valid ApplicationDto applicationDto,
                                                  @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = applicationFacade.updateApplication(applicationName, applicationDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @DeleteMapping(path = "/{applicationName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(@PathVariable("applicationName") String applicationName) {
        applicationFacade.deleteApplication(applicationName);
    }

    @GetMapping(path = "/{applicationName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationDto getSnapshot(@PathVariable String applicationName, @PathVariable Integer revision) {
        return applicationFacade.getSnapshot(applicationName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ApplicationDto> getAllAtRevision(@PathVariable Integer revision) {
        return applicationFacade.getAllAtRevision(revision);
    }
}
