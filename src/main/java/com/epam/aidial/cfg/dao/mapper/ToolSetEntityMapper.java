package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.ResourceAuthSettingsEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.SecuredResourceEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetSource;
import com.google.common.base.Objects;
import org.apache.commons.collections4.ListUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        DeploymentEntityMapper.class, ToolSetContainerEntityMapper.class, ResourceAuthSettingsEntityMapper.class
})
public abstract class ToolSetEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;
    @Autowired
    private ToolSetContainerEntityMapper toolSetContainerEntityMapper;
    @Autowired
    private ResourceAuthSettingsEntityMapper authSettingsEntityMapper;

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

    public ToolSetEntity toEntity(ToolSet domain, ToolSetEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        List<RoleShareResourceLimit> roleShareResourceLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleShareResourceLimits());
        List<RoleEntity> rolesForResourceShareLimits = deploymentEntityMapper.findRolesByNames(roleShareResourceLimits.stream().map(RoleShareResourceLimit::getRole).toList());

        ToolSetContainerEntity toolSetContainer = null;

        ToolSetSource source = domain.getSource();
        if (source instanceof ToolSetContainerSource containerSource) {
            toolSetContainer = toolSetContainerEntityMapper.toEntity(containerSource);
        }

        SecuredResource securedResource = domain.getDeployment();
        ResourceAuthSettings domainAuthSettings = securedResource != null ? securedResource.getAuthSettings() : null;

        SecuredResourceEntity securedResourceEntity = entity.getDeployment();
        ResourceAuthSettingsEntity entityAuthSettings = securedResourceEntity != null ? securedResourceEntity.getAuthSettings() : null;
        ResourceAuthSettings mappedEntityAuthSettings = authSettingsEntityMapper.toDomain(entityAuthSettings);

        boolean isMappedDefaultRoleLimitOrShareResourceLimitDiffer = deploymentEntityMapper
                .isMappedDefaultRoleLimitOrShareResourceLimitDiffer(securedResource, securedResourceEntity);

        ToolSetEntity updatedEntity = update(domain, entity);

        // Embedded entities update is not reflected in ToolSet's audit table, so provoking a dirty entity by manually setting 'updatedAt'
        if (!Objects.equal(domainAuthSettings, mappedEntityAuthSettings)
                || isMappedDefaultRoleLimitOrShareResourceLimitDiffer) {
            updatedEntity.setUpdatedAt(System.currentTimeMillis());
        }

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
