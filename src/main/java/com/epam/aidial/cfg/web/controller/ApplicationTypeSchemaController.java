package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

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
    public ResponseEntity<?> get(@RequestParam(name = "id", required = false) String id,
                                 @RequestHeader(value = "If-None-Match", required = false) String previousHash) {
        if (StringUtils.isEmpty(id)) {
            return ResponseEntity.ok(applicationTypeSchemaFacade.getAll());
        }
        if (StringUtils.isEmpty(previousHash)) {
            throw new IllegalArgumentException("Header 'If-None-Match' is required when 'id' parameter is provided");
        }
        var schemaDto = applicationTypeSchemaFacade.getSchemaWithHash(id);
        return responseEntityForGet(schemaDto.dto(), schemaDto.hash(), previousHash);
    }

    @GetMapping(path = "/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationTypeSchemaDto get(@RequestParam(name = "id") String id,
                                        @RequestParam(name = "revision") Integer revision) {
        return applicationTypeSchemaFacade.getSnapshot(id, revision);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createApplicationTypeSchema(@RequestBody @Valid ApplicationTypeSchemaDto schemaDto) {
        applicationTypeSchemaFacade.create(schemaDto);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(@RequestParam(name = "id") String id,
                                       @RequestBody @Valid ApplicationTypeSchemaDto dto,
                                       @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = applicationTypeSchemaFacade.update(id, dto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(name = "id") String id,
                       @RequestParam(name = "removeApplication", required = false, defaultValue = "true") boolean removeApplication) {
        applicationTypeSchemaFacade.delete(id, removeApplication);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ApplicationTypeSchemaDto> getAllAtRevision(@PathVariable Integer revision) {
        return applicationTypeSchemaFacade.getAllAtRevision(revision);
    }

    @GetMapping(path = "/core", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoreApplicationTypeSchema> getCore(@RequestParam(name = "id") String id,
                                                             @RequestHeader(value = "If-None-Match", required = false) String previousHash) {
        var coreWithDomainHash = applicationTypeSchemaFacade.getCoreSchemaWithHash(id);
        return responseEntityForGet(coreWithDomainHash.core(), coreWithDomainHash.hash(), previousHash);
    }

    @PutMapping(path = "/core", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(@RequestParam(name = "id") String id,
                                       @RequestBody @Valid CoreApplicationTypeSchema coreApplicationTypeSchema,
                                       @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = applicationTypeSchemaFacade.update(id, coreApplicationTypeSchema, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    private <T> ResponseEntity<T> responseEntityForGet(T obj, String newHash, String previousHash) {
        return newHash.equals(StringUtils.unwrap(previousHash, '"'))
                ? ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(newHash).build()
                : ResponseEntity.status(HttpStatus.OK).eTag(newHash).body(obj);
    }
}
