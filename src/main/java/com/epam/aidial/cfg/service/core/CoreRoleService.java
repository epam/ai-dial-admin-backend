package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException.OptimisticLockConflictExceptionDetails;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateResolver;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    private final EntitySyncStateResolver entitySyncStateResolver;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreRole> getCoreRoleWithHash(String roleName) {
        var roleWithHash = roleService.getRoleWithHash(roleName);
        var deployments = deploymentService.getAll();
        var coreRole = roleCoreMapper.mapRole(roleWithHash.model(), deployments);
        return new CoreWithDomainHash<>(coreRole, roleWithHash.hash());
    }

    @Transactional
    public String updateRole(String roleName, CoreRole coreRole, String hash) {
        assertHashNotNull(roleName, hash);

        var roleWithHash = roleService.getRoleWithHash(roleName);

        assertRoleWasNotUpdated(roleWithHash, hash, OptimisticLockConflictException::onUpdate);
        importCoreRole(roleName, coreRole);

        return roleService.getRoleWithHash(roleName).hash();
    }

    private void importCoreRole(String roleName, CoreRole coreRole) {
        Map<String, CoreRole> coreRoles = new HashMap<>(1);
        coreRoles.put(roleName, coreRole);

        Config config = new Config();
        config.setRoles(coreRoles);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String roleName, String hash) {
        assertHashNotNull(roleName, hash);

        var roleWithHash = roleService.getRoleWithHash(roleName);
        assertRoleWasNotUpdated(roleWithHash, hash, OptimisticLockConflictException::onGetSyncState);

        var role = roleWithHash.model();
        var deployments = deploymentService.getAll();
        var coreRole = roleCoreMapper.mapRole(role, deployments);

        return entitySyncStateResolver.resolve(
                coreRole,
                role.getUpdatedAt(),
                "roles",
                roleName
        );
    }

    private void assertHashNotNull(String roleName, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    String.format("Hash must not be null. Use \"*\" to skip optimistic check. Role:%s.", roleName)
            );
        }
    }

    private void assertRoleWasNotUpdated(DomainObjectWithHash<Role> roleWithHash,
                                         String expectedHash,
                                         Function<OptimisticLockConflictExceptionDetails, OptimisticLockConflictException> exceptionProvider) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        String currentHash = roleWithHash.hash();
        if (!expectedHash.equals(currentHash)) {
            String roleName = roleWithHash.model().getName();
            throw exceptionProvider.apply(new OptimisticLockConflictExceptionDetails("Role", roleName, expectedHash, currentHash));
        }
    }
}
