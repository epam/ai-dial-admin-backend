package com.epam.aidial.cfg.domain.mapper;

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
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleLimitMapper.class, FeatureCoreMapper.class
        }
)
public abstract class ModelCoreMapper {

    @Autowired
    private RoleLimitMapper roleLimitMapper;

    @Mapping(target = "descriptionKeywords", source = "topics")
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreModel mapModel(Model model);

    @Mapping(target = "deployment.name", source = "model.name")
    @Mapping(target = "topics", source = "model.descriptionKeywords")
    @Mapping(target = "deployment", ignore = true)
    @Mapping(target = "features", source = "model.features", qualifiedByName = "toFeaturesDto")
    public abstract Model mapModel(CoreModel model, Map<String, CoreRole> roles);

    @Mapping(target = "id", ignore = true)
    public abstract Upstream map(CoreUpstream upstream);

    public Map<String, String> map(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Objects::toString));
    }

    @AfterMapping
    public void mapRoles(@MappingTarget Model model, CoreModel coreEntity, Map<String, CoreRole> roles) {
        roleLimitMapper.mapRoles(model.getDeployment(), coreEntity.getUserRoles(), coreEntity.getName(), roles);
    }

}
