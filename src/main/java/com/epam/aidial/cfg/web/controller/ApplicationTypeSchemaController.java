package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/applicationTypeSchemas")
@Validated
@LogExecution
public class ApplicationTypeSchemaController {

    private final ApplicationTypeSchemaFacade applicationTypeSchemaFacade;

    public ApplicationTypeSchemaController(ApplicationTypeSchemaFacade applicationTypeSchemaFacade) {
        this.applicationTypeSchemaFacade = applicationTypeSchemaFacade;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ApplicationTypeSchemaDto> get(@RequestParam(name = "id", required = false) String id) {
        if (StringUtils.isEmpty(id)) {
            return applicationTypeSchemaFacade.getAll();
        }
        ApplicationTypeSchemaDto schemaDto = applicationTypeSchemaFacade.get(id);
        return schemaDto != null ? List.of(schemaDto) : Collections.emptyList();
    }

    @GetMapping(path = "/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationTypeSchemaDto get(@RequestParam(name = "id") String id,
                                        @RequestParam(name = "revision") Integer revision) {
        return applicationTypeSchemaFacade.getSnapshot(id, revision);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createApplicationTypeSchema(HttpServletResponse response,
                                            @RequestBody @Valid ApplicationTypeSchemaDto schemaDto) {
        applicationTypeSchemaFacade.create(schemaDto);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(HttpServletResponse response,
                       @RequestParam(name = "id") String id,
                       @RequestBody @Valid ApplicationTypeSchemaDto dto) {
        applicationTypeSchemaFacade.update(id, dto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(HttpServletResponse response,
                       @RequestParam(name = "id") String id,
                       @RequestParam(name = "removeApplication", required = false, defaultValue = "true") boolean removeApplication) {
        applicationTypeSchemaFacade.delete(id, removeApplication);
    }
}
