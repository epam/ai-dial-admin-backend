package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
public class RoleController {

    private final RoleFacade roleFacade;

    public RoleController(RoleFacade roleFacade) {
        this.roleFacade = roleFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<RoleDto> getAll(HttpServletResponse response) throws Exception {
        Collection<RoleDto> dtos = roleFacade.getAllRoles();
        return dtos;
    }

    @GetMapping(path = "/{roleName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDto getRole(HttpServletResponse response,
                           @PathVariable("roleName") String roleName) throws Exception {
        RoleDto dto = roleFacade.getRole(roleName);
        return dto;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createRole(HttpServletResponse response,
                           @RequestBody @Valid RoleDto roleDto) throws Exception {
        roleFacade.createRole(roleDto);
    }

    @DeleteMapping(path = "/{roleName}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(HttpServletResponse response, @PathVariable("roleName") String roleName) throws Exception {
        roleFacade.deleteRole(roleName);
    }

    @PutMapping(path = "/{roleName}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(HttpServletResponse response,
                           @PathVariable("roleName") String roleName,
                           @RequestBody @Valid RoleDto roleDto)
            throws Exception {
        roleFacade.updateRole(roleName, roleDto);
    }

    @GetMapping(path = "/{roleName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDto getSnapshot(@PathVariable String roleName, @PathVariable Integer revision) {
        return roleFacade.getSnapshot(roleName, revision);
    }
}
