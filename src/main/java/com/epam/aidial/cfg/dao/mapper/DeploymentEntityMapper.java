package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.ResourceAuthSettingsEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitId;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitEntity;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitId;
import com.epam.aidial.cfg.dao.model.SecuredResourceEntity;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.SetUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @Autowired
    private ResourceAuthSettingsEntityMapper resourceAuthSettingsEntityMapper;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    public abstract Deployment toDomain(DeploymentEntity deploymentEntity);

    @Mapping(target = "roleLimits", ignore = true)
    @Mapping(target = "roleShareResourceLimits", ignore = true)
    @Mapping(target = "type", ignore = true)
    public abstract DeploymentEntity toEntity(Deployment deployment, @MappingTarget DeploymentEntity entity);

    @Named("toSecuredResource")
    public SecuredResource toSecuredResource(DeploymentEntity deploymentEntity) {
        Deployment deployment = toDomain(deploymentEntity);
        if (!(deploymentEntity instanceof SecuredResourceEntity securedResourceEntity)) {
            throw new IllegalArgumentException("Only Secured Resource entity can be mapped to Secured Resource."
                    + " Deployment: %s".formatted(deployment.getName()));
        }

        ResourceAuthSettings authSettings = resourceAuthSettingsEntityMapper.toDomain(securedResourceEntity.getAuthSettings());

        return new SecuredResource(deployment, authSettings);
    }

    public void toSecuredResourceEntity(Deployment deployment, @MappingTarget SecuredResourceEntity entity) {
        DeploymentEntity mappedEntity = toEntity(deployment, entity);
        if (!(deployment instanceof SecuredResource securedResource)) {
            throw new IllegalArgumentException("Only Secured Resource can be mapped to Secured Resource entity."
                    + " Deployment: %s".formatted(mappedEntity.getName()));
        }

        entity.setName(mappedEntity.getName());
        entity.setIsPublic(mappedEntity.getIsPublic());
        entity.setRoleLimits(mappedEntity.getRoleLimits());
        entity.setRoleShareResourceLimits(mappedEntity.getRoleShareResourceLimits());
        entity.setDefaultRoleLimit(mappedEntity.getDefaultRoleLimit());
        entity.setDefaultRoleShareResourceLimit(mappedEntity.getDefaultRoleShareResourceLimit());

        ResourceAuthSettingsEntity authSettings = resourceAuthSettingsEntityMapper.toEntity(securedResource.getAuthSettings());
        entity.setAuthSettings(authSettings);
    }

    public List<RoleEntity> findRolesByNames(List<String> names) {
        if (names.isEmpty()) {
            return List.of();
        }

        List<RoleEntity> existingRoles = Lists.newArrayList(roleJpaRepository.findAllById(names));
        Set<String> existingRoleNames = existingRoles.stream().map(RoleEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingRoleNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find roles: " + namesDiff);
        }

        return existingRoles;
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
