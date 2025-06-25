package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AdapterEntityMapper {

    Adapter toDomain(AdapterEntity entity);

    @Mapping(target = "models", ignore = true)
    AdapterEntity toEntity(Adapter domain, @MappingTarget AdapterEntity entity);

    default List<String> mapModels(List<ModelEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(ModelEntity::getDeployment)
                .map(DeploymentEntity::getName)
                .collect(Collectors.toList());
    }
}
