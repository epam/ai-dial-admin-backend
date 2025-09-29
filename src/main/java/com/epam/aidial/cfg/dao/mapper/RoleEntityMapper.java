package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitId;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitId;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
        RoleLimitEntityMapper.class, RoleShareResourceLimitEntityMapper.class, CostLimitEntityMapper.class
})
public abstract class RoleEntityMapper {

    @Autowired
    protected RoleLimitEntityMapper roleLimitEntityMapper;
    @Autowired
    protected RoleShareResourceLimitEntityMapper roleShareResourceLimitEntityMapper;

    public abstract Role toDomain(RoleEntity entity);

    protected String mapKeyToString(KeyEntity value) {
        return value != null ? value.getName() : null;
    }

    public RoleEntity toEntity(Role domain,
                               RoleEntity entity,
                               List<KeyEntity> keyEntities,
                               List<RoleLimit> roleLimits,
                               List<DeploymentEntity> limitDeployments,
                               List<RoleShareResourceLimit> roleShareResourceLimits,
                               List<DeploymentEntity> roleShareDeployments) {
        RoleEntity updatedEntity = update(domain, entity);

        updatedEntity.getKeys().stream()
                .filter(key -> !keyEntities.contains(key))
                .forEach(key -> key.getRoles().remove(updatedEntity));
        keyEntities.stream()
                .filter(key -> !updatedEntity.getKeys().contains(key))
                .forEach(key -> key.getRoles().add(updatedEntity));
        updatedEntity.getKeys().clear();
        updatedEntity.getKeys().addAll(keyEntities);

        Map<RoleLimitId, RoleLimitEntity> existingRoleLimitEntitiesById = updatedEntity.getLimits().stream()
                .collect(Collectors.toMap(RoleLimitEntity::getId, Function.identity()));
        Map<String, DeploymentEntity> deploymentsByNames = limitDeployments.stream()
                .collect(Collectors.toMap(DeploymentEntity::getName, Function.identity()));
        List<RoleLimitEntity> roleLimitEntities = roleLimits.stream()
                .map(roleLimit -> mapToRoleLimitEntity(roleLimit, updatedEntity, deploymentsByNames, existingRoleLimitEntitiesById))
                .toList();
        updatedEntity.getLimits().clear();
        updatedEntity.getLimits().addAll(roleLimitEntities);

        Map<RoleShareResourceLimitId, RoleShareResourceLimitEntity> existingRoleShareResourceLimitEntitiesById = updatedEntity.getShare().stream()
                .collect(Collectors.toMap(RoleShareResourceLimitEntity::getId, Function.identity()));
        Map<String, DeploymentEntity> roleShareDeploymentsByNames = roleShareDeployments.stream()
                .collect(Collectors.toMap(DeploymentEntity::getName, Function.identity()));
        List<RoleShareResourceLimitEntity> roleShareResourceLimitEntities = roleShareResourceLimits.stream()
                .map(roleShareResourceLimit -> mapToRoleShareResourceLimitEntity(roleShareResourceLimit, updatedEntity,
                        roleShareDeploymentsByNames, existingRoleShareResourceLimitEntitiesById))
                .toList();
        updatedEntity.getShare().clear();
        updatedEntity.getShare().addAll(roleShareResourceLimitEntities);

        return updatedEntity;
    }

    private RoleLimitEntity mapToRoleLimitEntity(RoleLimit roleLimit,
                                                 RoleEntity role,
                                                 Map<String, DeploymentEntity> deploymentsByNames,
                                                 Map<RoleLimitId, RoleLimitEntity> existingRoleLimitEntitiesById) {
        DeploymentEntity deployment = deploymentsByNames.get(roleLimit.getDeploymentName());
        RoleLimitEntity roleLimitEntity = existingRoleLimitEntitiesById.getOrDefault(
                new RoleLimitId(deployment.getId(), role.getId()),
                new RoleLimitEntity()
        );
        return roleLimitEntityMapper.toEntity(roleLimit, role, deployment, roleLimitEntity);
    }

    private RoleShareResourceLimitEntity mapToRoleShareResourceLimitEntity(RoleShareResourceLimit roleShareResourceLimit,
                                                                           RoleEntity role,
                                                                           Map<String, DeploymentEntity> deploymentsByNames,
                                                                           Map<RoleShareResourceLimitId, RoleShareResourceLimitEntity> existingEntitiesById) {
        DeploymentEntity deployment = deploymentsByNames.get(roleShareResourceLimit.getDeploymentName());
        RoleShareResourceLimitEntity roleLimitEntity = existingEntitiesById.getOrDefault(
                new RoleShareResourceLimitId(deployment.getId(), role.getId()),
                new RoleShareResourceLimitEntity()
        );
        return roleShareResourceLimitEntityMapper.toEntity(roleShareResourceLimit, role, deployment, roleLimitEntity);
    }

    @Mapping(target = "limits", ignore = true)
    @Mapping(target = "share", ignore = true)
    @Mapping(target = "keys", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract RoleEntity update(Role domain, @MappingTarget RoleEntity entity);
}
