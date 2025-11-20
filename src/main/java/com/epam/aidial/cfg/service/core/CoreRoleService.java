package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service
@RequiredArgsConstructor
@Slf4j
public class CoreRoleService {

    private final RoleService roleService;
    private final RoleCoreMapper roleCoreMapper;
    private final DeploymentService deploymentService;
    private final ConfigImporter configImporter;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreRole> getCoreRoleWithHash(String roleName) {
        var roleWithHash = roleService.getRoleWithHash(roleName);
        var deployments = deploymentService.getAll();
        var coreRole = roleCoreMapper.mapRole(roleWithHash.model(), deployments);
        return new CoreWithDomainHash<>(coreRole, roleWithHash.hash());
    }

    @Transactional
    public String updateRole(String roleName, CoreRole coreRole, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    "Hash must not be null. Use \"*\" to skip optimistic check.");
        }

        var roleWithHash = roleService.getRoleWithHash(roleName);

        assertNotConcurrencyOverwrite(roleWithHash, hash);
        importCoreRole(roleName, coreRole);

        return roleService.getRoleWithHash(roleName).hash();
    }

    private void assertNotConcurrencyOverwrite(DomainObjectWithHash<Role> roleWithHash, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        Role role = roleWithHash.model();
        String currentHash = roleWithHash.hash();

        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: roleName={}, expectedHash={}, currentHash={}",
                    role.getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Optimistic lock conflict on update: roleName:'"
                    + "%s'. Please reload the data.", role.getName()));
        }
    }

    private void importCoreRole(String roleName, CoreRole coreRole) {
        Map<String, CoreRole> coreRoles = new HashMap<>(1);
        coreRoles.put(roleName, coreRole);

        Config config = new Config();
        config.setRoles(coreRoles);

        configImporter.importConfigWithOverride(config);
    }
}
