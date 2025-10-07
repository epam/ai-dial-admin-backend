package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AssistantEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import org.apache.commons.collections4.ListUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DeploymentEntityMapper.class, MapPropertiesMapper.class})
public abstract class AssistantEntityMapper {

    @Autowired
    protected DeploymentEntityMapper deploymentEntityMapper;

    @Mapping(target = "topics", source = "descriptionKeywords")
    public abstract Assistant toDomain(AssistantEntity entity);

    public AssistantEntity toEntity(Assistant domain, AssistantEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        boolean isMappedDefaultRoleLimitOrShareResourceLimitDiffer = deploymentEntityMapper
                .isDefaultRoleLimitDifferent(domain.getDeployment(), entity.getDeployment());

        AssistantEntity updatedEntity = update(domain, entity);

        if (isMappedDefaultRoleLimitOrShareResourceLimitDiffer) {
            updatedEntity.setUpdatedAt(System.currentTimeMillis());
        }

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.ASSISTANT);
        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "descriptionKeywords", source = "topics")
    public abstract AssistantEntity update(Assistant domain, @MappingTarget AssistantEntity entity);
}
