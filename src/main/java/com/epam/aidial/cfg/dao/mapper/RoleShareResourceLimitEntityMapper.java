package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RoleShareResourceLimitEntity;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = ShareResourceLimitEntityMapper.class)
public abstract class RoleShareResourceLimitEntityMapper {

    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "deploymentName", source = "deployment.name")
    public abstract RoleShareResourceLimit toDomain(RoleShareResourceLimitEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", source = "role")
    @Mapping(target = "deployment", source = "deployment")
    public abstract RoleShareResourceLimitEntity toEntity(RoleShareResourceLimit domain,
                                                          RoleEntity role,
                                                          DeploymentEntity deployment,
                                                          @MappingTarget RoleShareResourceLimitEntity roleShareResourceLimitEntity);
}