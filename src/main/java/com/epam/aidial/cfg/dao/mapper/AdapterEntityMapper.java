package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class AdapterEntityMapper {

    @Autowired
    private ModelJpaRepository modelJpaRepository;

    public abstract Adapter toDomain(AdapterEntity entity);

    public AdapterEntity toEntity(Adapter domain, AdapterEntity entity) {
        boolean shouldUpdateModels = domain.getModels() != null;
        List<ModelEntity> models = shouldUpdateModels
                ? findModelsByNames(domain.getModels())
                : entity.getModels();

        AdapterEntity updatedEntity = update(domain, entity);

        if (shouldUpdateModels) {
            updatedEntity.getModels().forEach(model -> {
                model.setAdapter(null);
                model.setEndpoint(ModelEndpointUtils.concatEndpointAndPath(updatedEntity.getBaseEndpoint(), model.getAdapterCompletionEndpointPath()));
            });
            models.forEach(app -> {
                app.setAdapter(updatedEntity);
                app.setEndpoint(null);
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

    private List<ModelEntity> findModelsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<ModelEntity> existingModels = Lists.newArrayList(modelJpaRepository.findAllById(names));
        Set<String> existingModelsNames = existingModels.stream()
                .map(ModelEntity::getId)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingModelsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find Models: " + namesDiff);
        }

        return existingModels;
    }
}
