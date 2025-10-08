package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/v1/roles")
@Validated
@LogExecution
@Slf4j
@RequiredArgsConstructor
public class RoleController {

    private final RoleFacade roleFacade;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<RoleDto> getAll() {
        return roleFacade.getAllRoles();
    }

    @GetMapping(path = "/{roleName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDto getRole(@PathVariable("roleName") String roleName) {
        return roleFacade.getRole(roleName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createRole(@RequestBody @Valid RoleDto roleDto) {
        roleFacade.createRole(roleDto);
    }

    @DeleteMapping(path = "/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable("roleName") String roleName) throws Exception {
        roleFacade.deleteRole(roleName);
    }

    @PutMapping(path = "/{roleName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(@PathVariable("roleName") String roleName, @RequestBody @Valid RoleDto roleDto) {
        roleFacade.updateRole(roleName, roleDto);
    }

    @GetMapping(path = "/{roleName}/revision/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDto getSnapshot(@PathVariable String roleName, @PathVariable Integer revision) {
        return roleFacade.getSnapshot(roleName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<RoleDto> getAllAtRevision(@PathVariable Integer revision) throws Exception {
        return roleFacade.getAllAtRevision(revision);
    }
}
