package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.service.transfer.importer.ConfigImporter;
import com.epam.aidial.cfg.web.facade.mapper.RoleDtoMapper;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class RoleFacade {

    private final RoleService roleService;
    private final DeploymentService deploymentService;
    private final RoleDtoMapper mapper;
    private final RoleCoreMapper roleCoreMapper;
    private final ConfigImporter configImporter;

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

    public DtoWithDomainHash<RoleDto> getRoleWithHash(String roleName) {
        var roleWithHash = roleService.getRoleWithHash(roleName);
        return new DtoWithDomainHash<>(mapper.toDto(roleWithHash.model()), roleWithHash.hash());
    }

    public void createRole(RoleDto roleDto) {
        Optional.of(roleDto)
                .map(mapper::toDomain)
                .ifPresent(roleService::createRole);
    }

    public String updateRole(String roleName, RoleDto roleDto, String hash) {
        Role role = mapper.toDomain(roleDto);
        return roleService.updateRole(roleName, role, hash);
    }

    public void deleteRole(String role) {
        roleService.deleteRole(role);
    }

    public RoleDto getSnapshot(String roleName, Integer revision) {
        Role role = roleService.getSnapshot(roleName, revision);
        return mapper.toDto(role);
    }

    public Collection<RoleDto> getAllAtRevision(Integer revision) {
        return roleService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public CoreRole getCoreRole(String roleName) {
        Role role = roleService.getRole(roleName);
        Collection<Deployment> deployments = deploymentService.getAll();
        return roleCoreMapper.mapRole(role, deployments);
    }

    public void updateCoreRole(String roleName, CoreRole coreRole) {
        roleService.assertExists(roleName);

        Map<String, CoreRole> coreRoles = new HashMap<>(1);
        coreRoles.put(roleName, coreRole);

        Config config = new Config();
        config.setRoles(coreRoles);

        configImporter.importConfigWithOverride(config);
    }
}
