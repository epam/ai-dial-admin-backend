package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreUpstream;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class, FeatureCoreMapper.class
        }
)
public abstract class ModelCoreMapper {

    @Autowired
    protected ModelEndpointMapper modelEndpointMapper;

    @Mapping(target = "descriptionKeywords", source = "topics")
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    @Mapping(target = "endpoint", ignore = true)
    public abstract CoreModel mapModel(Model model);

    @Mapping(target = "deployment", source = "model")
    @Mapping(target = "displayName", source = "model.displayName")
    @Mapping(target = "description", source = "model.description")
    @Mapping(target = "topics", source = "model.descriptionKeywords")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "features", source = "model.features")
    public abstract Model mapModel(CoreModel model, Adapter adapter, String endpointDeploymentName, @Context ShareResourceLimit defaultShareResourceLimit);

    @Mapping(target = "id", ignore = true)
    abstract Upstream map(CoreUpstream upstream);

    @AfterMapping
    void afterMapping(@MappingTarget CoreModel coreEntity, Model model) {
        coreEntity.setEndpoint(modelEndpointMapper.mapModelToEndpoint(model));
    }
}
