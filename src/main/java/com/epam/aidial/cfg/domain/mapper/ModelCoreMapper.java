package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreUpstream;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class, FeatureCoreMapper.class
        }
)
public abstract class ModelCoreMapper {

    @Mapping(target = "descriptionKeywords", source = "model.topics")
    @Mapping(target = "name", source = "model.deployment.name")
    @Mapping(target = "userRoles", source = "model.deployment")
    @Mapping(target = "endpoint", source = "endpoint")
    public abstract CoreModel mapModel(Model model, String endpoint);

    @Mapping(target = "descriptionKeywords", source = "topics")
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreModel mapModel(Model model);

    @Mapping(target = "deployment", source = "coreModel", qualifiedByName = "toDeployment")
    @Mapping(target = "displayName", source = "coreModel.displayName")
    @Mapping(target = "description", source = "coreModel.description")
    @Mapping(target = "endpoint", source = "coreModel.endpoint")
    @Mapping(target = "topics", source = "coreModel.descriptionKeywords")
    @Mapping(target = "features", source = "coreModel.features")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Model mapModel(CoreModel coreModel,
                                   ModelSource source,
                                   @Context List<RoleLimit> roleLimits,
                                   @MappingTarget Model model);

    @Mapping(target = "id", ignore = true)
    abstract Upstream map(CoreUpstream upstream);

    public abstract Model copy(Model model);
}
