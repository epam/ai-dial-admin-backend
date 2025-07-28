package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
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
        Long createdAt = entity.getCreatedAt();

        AdapterEntity updatedEntity = update(domain, entity);

        updatedEntity.setCreatedAt(
                updatedEntity.getCreatedAt() == null ? createdAt : updatedEntity.getCreatedAt()
        );

        List<String> modelNames = domain.getModels();
        List<ModelEntity> models = Lists.newArrayList(modelJpaRepository.findAllById(modelNames));
        Set<String> existingModels = models.stream().map(ModelEntity::getDeploymentName).collect(Collectors.toSet());
        Set<String> namesDiff = SetUtils.difference(new HashSet<>(modelNames), existingModels);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find models: " + namesDiff);
        }

        for (ModelEntity model : updatedEntity.getModels()) {
            model.setAdapter(null);
        }
        updatedEntity.getModels().clear();
        updatedEntity.getModels().addAll(models);
        for (ModelEntity model : models) {
            model.setAdapter(entity);
        }
        return updatedEntity;
    }

    @Mapping(target = "models", ignore = true)
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
