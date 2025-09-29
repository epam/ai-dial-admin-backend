package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class AdapterEntityMapper {

    public abstract Adapter toDomain(AdapterEntity entity);

    public AdapterEntity toEntity(Adapter domain, AdapterEntity entity, List<ModelEntity> models) {
        AdapterEntity updatedEntity = update(domain, entity);

        boolean shouldUpdateModels = models != null;
        if (shouldUpdateModels) {
            updatedEntity.getModels().stream()
                    .filter(model -> !models.contains(model))
                    .forEach(model -> {
                        model.setAdapter(null);
                        model.setEndpoint(ModelEndpointUtils.concatEndpointAndPath(updatedEntity.getBaseEndpoint(), model.getAdapterCompletionEndpointPath()));
                    });
            models.stream()
                    .filter(model -> !updatedEntity.getModels().contains(model))
                    .forEach(model -> {
                        model.setAdapter(updatedEntity);
                        model.setEndpoint(null);
                    });
            updatedEntity.getModels().clear();
            updatedEntity.getModels().addAll(models);
        }

        return updatedEntity;
    }

    @Mapping(target = "models", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract AdapterEntity update(Adapter domain, @MappingTarget AdapterEntity entity);

    public List<String> mapModels(List<ModelEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(ModelEntity::getDeployment)
                .map(DeploymentEntity::getName)
                .collect(Collectors.toList());
    }
}
