package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AddonEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import org.apache.commons.collections4.ListUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DeploymentEntityMapper.class})
public abstract class AddonEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    public abstract Addon toDomain(AddonEntity entity);

    public AddonEntity toEntity(Addon domain, AddonEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        List<RoleShareResourceLimit> roleShareResourceLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleShareResourceLimits());
        List<RoleEntity> rolesForResourceShareLimits = deploymentEntityMapper.findRolesByNames(roleShareResourceLimits.stream().map(RoleShareResourceLimit::getRole).toList());

        boolean isMappedDefaultRoleLimitOrShareResourceLimitDiffer = deploymentEntityMapper
                .isMappedDefaultRoleLimitOrShareResourceLimitDiffer(domain.getDeployment(), entity.getDeployment());

        AddonEntity updatedEntity = update(domain, entity);

        if (isMappedDefaultRoleLimitOrShareResourceLimitDiffer) {
            updatedEntity.setUpdatedAt(System.currentTimeMillis());
        }

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        deploymentEntityMapper.setRoleShareResourceLimits(updatedEntity.getDeployment(), rolesForResourceShareLimits, roleShareResourceLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.ADDON);
        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    protected abstract AddonEntity update(Addon domain, @MappingTarget AddonEntity entity);

}
