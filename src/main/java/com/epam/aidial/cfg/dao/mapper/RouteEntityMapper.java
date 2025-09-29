package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.route.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DeploymentEntityMapper.class, UpstreamEntityMapper.class})
public abstract class RouteEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    public abstract Route toDomain(RouteEntity entity);

    public RouteEntity toEntity(Route domain,
                                RouteEntity entity,
                                List<RoleLimit> roleLimits,
                                List<RoleEntity> rolesForLimits,
                                List<RoleShareResourceLimit> roleShareResourceLimits,
                                List<RoleEntity> rolesForResourceShareLimits) {
        RouteEntity updatedEntity = update(domain, entity);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        deploymentEntityMapper.setRoleShareResourceLimits(updatedEntity.getDeployment(), rolesForResourceShareLimits, roleShareResourceLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.ROUTE);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    abstract RouteEntity update(Route domain, @MappingTarget RouteEntity entity);
}
