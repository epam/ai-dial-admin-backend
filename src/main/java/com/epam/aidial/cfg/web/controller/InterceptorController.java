package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.security.FullAdminOnly;
import com.epam.aidial.core.config.CoreInterceptor;
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
@RequestMapping("/api/v1/interceptors")
@Validated
@LogExecution
public class InterceptorController extends AbstractController {

    private final InterceptorFacade interceptorFacade;

    public InterceptorController(InterceptorFacade interceptorFacade) {
        this.interceptorFacade = interceptorFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<InterceptorDto> getAll() {
        return interceptorFacade.getAllInterceptors();
    }

    @GetMapping(path = "/{interceptorName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InterceptorDto> getInterceptor(@PathVariable("interceptorName") String interceptorName,
                                                         @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = interceptorFacade.getInterceptorWithHash(interceptorName);
        return responseEntityForGet(dtoWithHash.dto(), dtoWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/core/{interceptorName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoreInterceptor> getCoreInterceptor(@PathVariable String interceptorName,
                                                              @RequestHeader(value = "If-None-Match") String previousHash) {
        var coreWithHash = interceptorFacade.getCoreInterceptorWithHash(interceptorName);
        return responseEntityForGet(coreWithHash.core(), coreWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/{interceptorName}/sync-state", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntitySyncStateDto getSyncState(@PathVariable String interceptorName,
                                           @RequestHeader(value = "If-Match") String previousHash) {
        return interceptorFacade.getSyncState(interceptorName, StringUtils.unwrap(previousHash, '"'));
    }

    @FullAdminOnly
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createInterceptor(@RequestBody @Valid InterceptorDto interceptorDto) {
        interceptorFacade.createInterceptor(interceptorDto);
    }

    @FullAdminOnly
    @PutMapping(path = "/{interceptorName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateInterceptor(@PathVariable("interceptorName") String interceptorName,
                                                  @RequestBody @Valid InterceptorDto interceptorDto,
                                                  @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = interceptorFacade.updateInterceptor(interceptorName, interceptorDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @FullAdminOnly
    @PutMapping(path = "/core/{interceptorName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateInterceptor(@PathVariable String interceptorName,
                                                  @RequestBody @Valid CoreInterceptor coreInterceptor,
                                                  @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = interceptorFacade.updateInterceptor(interceptorName, coreInterceptor, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @FullAdminOnly
    @DeleteMapping(path = "/{interceptorName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInterceptor(@PathVariable("interceptorName") String interceptorName) {
        interceptorFacade.deleteInterceptor(interceptorName);
    }


    @GetMapping(path = "/{interceptorName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public InterceptorDto getSnapshot(@PathVariable String interceptorName, @PathVariable Integer revision) {
        return interceptorFacade.getSnapshot(interceptorName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<InterceptorDto> getAllAtRevision(@PathVariable Integer revision) {
        return interceptorFacade.getAllAtRevision(revision);
    }
}
