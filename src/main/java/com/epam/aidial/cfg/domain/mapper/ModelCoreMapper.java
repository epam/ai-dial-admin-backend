package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreUpstream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    @Mapping(target = "deployment", source = "model")
    @Mapping(target = "displayName", source = "model.displayName")
    @Mapping(target = "description", source = "model.description")
    @Mapping(target = "endpoint", source = "model.endpoint")
    @Mapping(target = "topics", source = "model.descriptionKeywords")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "features", source = "model.features")
    public abstract Model mapModel(CoreModel model, ModelSource source);

    @Mapping(target = "id", ignore = true)
    abstract Upstream map(CoreUpstream upstream);
}
