package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.web.facade.mapper.RoleDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class RoleFacade {

    private final RoleService roleService;
    private final RoleDtoMapper mapper;

    public Collection<RoleDto> getAllRoles() {
        return roleService.getAllRoles()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public RoleDto getRole(String roleName) {
        Role role = roleService.getRole(roleName);
        return mapper.toDto(role);
    }

    public void createRole(RoleDto roleDto) {
        Optional.of(roleDto)
                .map(mapper::toDomain)
                .ifPresent(roleService::createRole);
    }

    public void updateRole(String roleName, RoleDto roleDto) {
        Role value = mapper.toDomain(roleDto);
        roleService.updateRole(roleName, value);
    }

    public void deleteRole(String role) {
        roleService.deleteRole(role);
    }

    public RoleDto getSnapshot(String roleName, Integer revision) {
        Role role = roleService.getSnapshot(roleName, revision);
        return mapper.toDto(role);
    }
}
