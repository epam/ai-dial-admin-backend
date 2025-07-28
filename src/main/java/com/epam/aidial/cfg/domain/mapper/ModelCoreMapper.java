package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreUpstream;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleLimitMapper.class, FeatureCoreMapper.class
        }
)
public abstract class ModelCoreMapper {

    @Autowired
    protected ModelEndpointMapper modelEndpointMapper;

    @Autowired
    private RoleLimitMapper roleLimitMapper;

    @Mapping(target = "descriptionKeywords", source = "topics")
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    @Mapping(target = "endpoint", ignore = true)
    public abstract CoreModel mapModel(Model model);

    @Mapping(target = "deployment.name", source = "model.name")
    @Mapping(target = "displayName", source = "model.displayName")
    @Mapping(target = "description", source = "model.description")
    @Mapping(target = "topics", source = "model.descriptionKeywords")
    @Mapping(target = "createdAt", source = "model.createdAt")
    @Mapping(target = "updatedAt", source = "model.updatedAt")
    @Mapping(target = "deployment", ignore = true)
    @Mapping(target = "features", source = "model.features", qualifiedByName = "toFeaturesDto")
    public abstract Model mapModel(CoreModel model, Map<String, CoreRole> roles, Adapter adapter, String endpointDeploymentName);

    @Mapping(target = "id", ignore = true)
    public abstract Upstream map(CoreUpstream upstream);

    @AfterMapping
    public void mapRoles(@MappingTarget Model model, CoreModel coreEntity, Map<String, CoreRole> roles) {
        roleLimitMapper.mapRoles(model.getDeployment(), coreEntity.getUserRoles(), coreEntity.getName(), roles);
    }

    @AfterMapping
    public void afterMapping(@MappingTarget CoreModel coreEntity, Model model) {
        coreEntity.setEndpoint(modelEndpointMapper.mapModelToEndpoint(model));
    }
}
