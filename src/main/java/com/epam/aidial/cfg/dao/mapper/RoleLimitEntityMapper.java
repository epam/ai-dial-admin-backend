package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = LimitEntityMapper.class)
public abstract class RoleLimitEntityMapper {

    @Mapping(target = "role", source = "id.roleName")
    @Mapping(target = "deploymentName", source = "id.deploymentName")
    public abstract RoleLimit toDomain(RoleLimitEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", source = "role")
    @Mapping(target = "deployment", source = "deployment")
    public abstract RoleLimitEntity toEntity(RoleLimit domain,
                                             RoleEntity role,
                                             DeploymentEntity deployment,
                                             @MappingTarget RoleLimitEntity roleLimitEntity);
}
