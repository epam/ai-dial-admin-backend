package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitId;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitId;
import com.epam.aidial.cfg.dao.model.SecuredResourceEntity;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
        RoleLimitEntityMapper.class, LimitEntityMapper.class, RoleShareResourceLimitEntityMapper.class,
        ShareResourceLimitEntityMapper.class, ResourceAuthSettingsEntityMapper.class
})
public abstract class DeploymentEntityMapper {

    @Autowired
    private RoleLimitEntityMapper roleLimitEntityMapper;
    @Autowired
    private RoleShareResourceLimitEntityMapper roleShareResourceLimitEntityMapper;

    public abstract Deployment toDomain(DeploymentEntity deploymentEntity);

    public abstract SecuredResource toDomain(SecuredResourceEntity deploymentEntity);

    @ToEntity
    public abstract DeploymentEntity toEntity(Deployment deployment, @MappingTarget DeploymentEntity entity);

    @ToEntity
    public abstract SecuredResourceEntity toEntity(SecuredResource deployment, @MappingTarget SecuredResourceEntity entity);

    @Mapping(target = "roleLimits", ignore = true)
    @Mapping(target = "roleShareResourceLimits", ignore = true)
    @Mapping(target = "type", ignore = true)
    public @interface ToEntity {
    }

    public void setRoleLimits(DeploymentEntity deployment, List<RoleEntity> roles, List<RoleLimit> roleLimits) {
        Map<RoleLimitId, RoleLimitEntity> existingRoleLimitEntitiesById = deployment.getRoleLimits().stream()
                .collect(Collectors.toMap(RoleLimitEntity::getId, Function.identity()));
        Map<String, RoleEntity> rolesByNames = roles.stream()
                .collect(Collectors.toMap(RoleEntity::getName, Function.identity()));
        List<RoleLimitEntity> roleLimitEntities = roleLimits.stream()
                .map(roleLimit -> mapToRoleLimitEntity(roleLimit, deployment, rolesByNames, existingRoleLimitEntitiesById))
                .toList();

        deployment.getRoleLimits().clear();
        deployment.getRoleLimits().addAll(roleLimitEntities);
    }

    public void setRoleShareResourceLimits(DeploymentEntity deployment, List<RoleEntity> roles, List<RoleShareResourceLimit> roleShareResourceLimits) {
        Map<RoleShareResourceLimitId, RoleShareResourceLimitEntity> existingRoleShareResourceLimitEntitiesById = deployment.getRoleShareResourceLimits().stream()
                .collect(Collectors.toMap(RoleShareResourceLimitEntity::getId, Function.identity()));
        Map<String, RoleEntity> rolesByNames = roles.stream()
                .collect(Collectors.toMap(RoleEntity::getName, Function.identity()));
        List<RoleShareResourceLimitEntity> roleShareResourceLimitEntities = roleShareResourceLimits.stream()
                .map(roleShareResourceLimit -> mapToRoleShareResourceLimitEntity(roleShareResourceLimit, deployment, rolesByNames, existingRoleShareResourceLimitEntitiesById))
                .toList();

        deployment.getRoleShareResourceLimits().clear();
        deployment.getRoleShareResourceLimits().addAll(roleShareResourceLimitEntities);
    }

    @AfterMapping
    public void afterMapping(@MappingTarget Deployment deployment) {
        if (deployment.getDefaultRoleLimit() == null) {
            deployment.setDefaultRoleLimit(new Limit());
        }
        if (deployment.getDefaultRoleShareResourceLimit() == null) {
            deployment.setDefaultRoleShareResourceLimit(new ShareResourceLimit());
        }
        if (deployment.getRoleLimits() != null) {
            for (RoleLimit roleLimit : deployment.getRoleLimits()) {
                if (roleLimit.getLimit() == null) {
                    roleLimit.setLimit(new Limit());
                }
            }
        }
        if (deployment.getRoleShareResourceLimits() != null) {
            for (RoleShareResourceLimit roleShareResourceLimit : deployment.getRoleShareResourceLimits()) {
                if (roleShareResourceLimit.getLimit() == null) {
                    roleShareResourceLimit.setLimit(new ShareResourceLimit());
                }
            }
        }
    }

    private RoleLimitEntity mapToRoleLimitEntity(RoleLimit roleLimit,
                                                 DeploymentEntity deployment,
                                                 Map<String, RoleEntity> rolesByNames,
                                                 Map<RoleLimitId, RoleLimitEntity> existingRoleLimitEntitiesById) {
        RoleEntity role = rolesByNames.get(roleLimit.getRole());
        RoleLimitEntity roleLimitEntity = existingRoleLimitEntitiesById.getOrDefault(
                new RoleLimitId(deployment.getId(), role.getId()),
                new RoleLimitEntity()
        );
        return roleLimitEntityMapper.toEntity(roleLimit, role, deployment, roleLimitEntity);
    }

    private RoleShareResourceLimitEntity mapToRoleShareResourceLimitEntity(RoleShareResourceLimit roleShareResourceLimit,
                                                                           DeploymentEntity deployment,
                                                                           Map<String, RoleEntity> rolesByNames,
                                                                           Map<RoleShareResourceLimitId, RoleShareResourceLimitEntity> existingRoleShareResourceLimitEntitiesById) {
        RoleEntity role = rolesByNames.get(roleShareResourceLimit.getRole());
        RoleShareResourceLimitEntity roleShareResourceLimitEntity = existingRoleShareResourceLimitEntitiesById.getOrDefault(
                new RoleShareResourceLimitId(deployment.getId(), role.getId()),
                new RoleShareResourceLimitEntity()
        );
        return roleShareResourceLimitEntityMapper.toEntity(roleShareResourceLimit, role, deployment, roleShareResourceLimitEntity);
    }
}
