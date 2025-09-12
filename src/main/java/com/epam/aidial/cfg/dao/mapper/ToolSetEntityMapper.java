package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ToolSet;
import org.apache.commons.collections4.ListUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = DeploymentEntityMapper.class)
public abstract class ToolSetEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    @Mapping(target = "deployment", source = "deployment", qualifiedByName = "toSecuredResource")
    public abstract ToolSet toDomain(ToolSetEntity entity);

    public ToolSetEntity toEntity(ToolSet domain, ToolSetEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        List<RoleShareResourceLimit> roleShareResourceLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleShareResourceLimits());
        List<RoleEntity> rolesForResourceShareLimits = deploymentEntityMapper.findRolesByNames(roleShareResourceLimits.stream().map(RoleShareResourceLimit::getRole).toList());

        ToolSetEntity updatedEntity = update(domain, entity);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        deploymentEntityMapper.setRoleShareResourceLimits(updatedEntity.getDeployment(), rolesForResourceShareLimits, roleShareResourceLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.TOOL_SET);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ToolSetEntity update(ToolSet domain, @MappingTarget ToolSetEntity entity);
}
