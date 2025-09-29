package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.ToolSetEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetSource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        DeploymentEntityMapper.class, ToolSetContainerEntityMapper.class
})
public abstract class ToolSetEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;
    @Autowired
    private ToolSetContainerEntityMapper toolSetContainerEntityMapper;

    @Mapping(target = "source", source = "entity", qualifiedByName = "mapSource")
    public abstract ToolSet toDomain(ToolSetEntity entity);

    @Named("mapSource")
    protected ToolSetSource mapSource(ToolSetEntity entity) {
        ToolSetContainerEntity containerEntity = entity.getToolSetContainer();
        if (containerEntity != null) {
            return toolSetContainerEntityMapper.toDomain(containerEntity);
        }
        return new ToolSetEndpointsSource();
    }

    public ToolSetEntity toEntity(ToolSet domain,
                                  ToolSetEntity entity,
                                  ToolSetContainerEntity toolSetContainer,
                                  List<RoleLimit> roleLimits,
                                  List<RoleEntity> rolesForLimits,
                                  List<RoleShareResourceLimit> roleShareResourceLimits,
                                  List<RoleEntity> rolesForResourceShareLimits) {
        ToolSetEntity updatedEntity = update(domain, entity);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        deploymentEntityMapper.setRoleShareResourceLimits(updatedEntity.getDeployment(), rolesForResourceShareLimits, roleShareResourceLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.TOOL_SET);

        updatedEntity.setToolSetContainer(toolSetContainer);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "toolSetContainer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ToolSetEntity update(ToolSet domain, @MappingTarget ToolSetEntity entity);
}
