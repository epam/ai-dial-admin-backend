package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
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
@RequestMapping("/api/v1/applications")
@Validated
@LogExecution
public class ApplicationController {

    private final ApplicationFacade applicationFacade;

    public ApplicationController(ApplicationFacade applicationFacade) {
        this.applicationFacade = applicationFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ApplicationInfoDto> getAllApplications(HttpServletResponse response) {
        return applicationFacade.getAllApplications();
    }

    @GetMapping(path = "/{applicationName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationDto getApplication(HttpServletResponse response,
                                         @PathVariable("applicationName") String applicationName) {
        return applicationFacade.getApplication(applicationName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createApplication(HttpServletResponse response,
                                  @RequestBody @Valid ApplicationDto applicationDto) {
        applicationFacade.createApplication(applicationDto);
    }

    @PutMapping(path = "/{applicationName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateApplication(HttpServletResponse response,
                                  @PathVariable("applicationName") String applicationName,
                                  @RequestBody @Valid ApplicationDto applicationDto) {
        applicationFacade.updateApplication(applicationName, applicationDto);
    }

    @DeleteMapping(path = "/{applicationName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(HttpServletResponse response,
                                  @PathVariable("applicationName") String applicationName) {
        applicationFacade.deleteApplication(applicationName);
    }

    @GetMapping(path = "/{applicationName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationDto getSnapshot(@PathVariable String applicationName, @PathVariable Integer revision) {
        return applicationFacade.getSnapshot(applicationName, revision);
    }
}
