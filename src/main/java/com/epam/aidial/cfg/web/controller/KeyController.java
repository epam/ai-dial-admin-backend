package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.core.config.CoreKey;
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
@RequestMapping("/api/v1/keys")
@Validated
@LogExecution
public class KeyController extends AbstractController {

    private final KeyFacade keyFacade;

    public KeyController(KeyFacade keyFacade) {
        this.keyFacade = keyFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<KeyDto> getAllKeys() {
        return keyFacade.getAllKeys();
    }

    @GetMapping(path = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyDto> getKey(@PathVariable String name,
                                         @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = keyFacade.getKeyWithHash(name);
        return responseEntityForGet(dtoWithHash.dto(), dtoWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/core/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoreKey> getCoreKey(@PathVariable String name,
                                              @RequestHeader(value = "If-None-Match") String previousHash) {
        var coreWithHash = keyFacade.getCoreKeyWithHash(name);
        return responseEntityForGet(coreWithHash.core(), coreWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/{name}/sync-state", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntitySyncStateDto getSyncState(@PathVariable String name,
                                           @RequestHeader(value = "If-Match") String previousHash) {
        return keyFacade.getSyncState(name, StringUtils.unwrap(previousHash, '"'));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createKey(@RequestBody @Valid KeyDto keyDto) {
        keyFacade.createKey(keyDto);
    }

    @PutMapping(path = "/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateKey(@PathVariable String name,
                                          @RequestBody @Valid KeyDto keyDto,
                                          @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = keyFacade.updateKey(name, keyDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @PutMapping(path = "/core/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateKey(@PathVariable String name,
                                          @RequestBody @Valid CoreKey coreKey,
                                          @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = keyFacade.updateKey(name, coreKey, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @DeleteMapping(path = "/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKey(@PathVariable String name) {
        keyFacade.deleteKey(name);
    }

    @GetMapping(path = "/{name}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public KeyDto getSnapshot(@PathVariable String name, @PathVariable Integer revision) {
        return keyFacade.getSnapshot(name, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<KeyDto> getAllAtRevision(@PathVariable Integer revision) throws Exception {
        return keyFacade.getAllAtRevision(revision);
    }
}
