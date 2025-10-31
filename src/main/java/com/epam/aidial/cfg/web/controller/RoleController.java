package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.core.config.CoreRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/v1/roles")
@Validated
@LogExecution
@Slf4j
@RequiredArgsConstructor
public class RoleController extends AbstractController {

    private final RoleFacade roleFacade;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<RoleDto> getAll() {
        return roleFacade.getAllRoles();
    }

    @GetMapping(path = "/{roleName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDto> getRole(@PathVariable("roleName") String roleName,
                                           @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = roleFacade.getRoleWithHash(roleName);
        return responseEntityForGet(dtoWithHash.dto(), dtoWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/core/{roleName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoreRole> getCoreRole(@PathVariable String roleName,
                                                @RequestHeader(value = "If-None-Match") String previousHash) {
        var coreWithHash = roleFacade.getCoreRoleWithHash(roleName);
        return responseEntityForGet(coreWithHash.core(), coreWithHash.hash(), previousHash);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createRole(@RequestBody @Valid RoleDto roleDto) {
        roleFacade.createRole(roleDto);
    }

    @DeleteMapping(path = "/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable("roleName") String roleName) {
        roleFacade.deleteRole(roleName);
    }

    @PutMapping(path = "/{roleName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateRole(@PathVariable("roleName") String roleName,
                                           @RequestBody @Valid RoleDto roleDto,
                                           @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = roleFacade.updateRole(roleName, roleDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @PutMapping(path = "/core/{roleName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateRole(@PathVariable String roleName,
                                           @RequestBody @Valid CoreRole coreRole,
                                           @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = roleFacade.updateRole(roleName, coreRole, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @GetMapping(path = "/{roleName}/revision/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDto getSnapshot(@PathVariable String roleName, @PathVariable Integer revision) {
        return roleFacade.getSnapshot(roleName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<RoleDto> getAllAtRevision(@PathVariable Integer revision) {
        return roleFacade.getAllAtRevision(revision);
    }
}
