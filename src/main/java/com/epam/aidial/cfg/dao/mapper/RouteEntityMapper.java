package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.Route;
import org.apache.commons.collections4.ListUtils;
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

    public RouteEntity toEntity(Route domain, RouteEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> roles = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());
        Long createdAt = entity.getCreatedAt();

        RouteEntity updatedEntity = update(domain, entity);

        updatedEntity.setCreatedAt(
                updatedEntity.getCreatedAt() == null ? createdAt : updatedEntity.getCreatedAt()
        );

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), roles, roleLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.ROUTE);
        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    public abstract RouteEntity update(Route domain, @MappingTarget RouteEntity entity);
}
