package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AdapterContainerEntity;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.source.AdapterEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = AdapterContainerEntityMapper.class)
public abstract class AdapterEntityMapper {

    @Autowired
    protected AdapterContainerEntityMapper adapterContainerEntityMapper;

    @Mapping(target = "source", source = "entity", qualifiedByName = "mapSource")
    public abstract Adapter toDomain(AdapterEntity entity);

    @Named("mapSource")
    protected AdapterSource mapSource(AdapterEntity entity) {
        AdapterContainerEntity containerEntity = entity.getAdapterContainer();
        if (containerEntity != null) {
            return adapterContainerEntityMapper.toDomain(containerEntity);
        }
        return new AdapterEndpointsSource();
    }

    public AdapterEntity toEntity(Adapter domain,
                                  AdapterEntity entity,
                                  List<ModelEntity> models,
                                  AdapterContainerEntity adapterContainer) {
        AdapterEntity updatedEntity = update(domain, entity);
        updatedEntity.setAdapterContainer(adapterContainer);

        boolean shouldUpdateModels = models != null;
        if (shouldUpdateModels) {
            updatedEntity.getModels().stream()
                    .filter(model -> !models.contains(model))
                    .forEach(model -> {
                        model.setAdapter(null);
                        model.setEndpoint(ModelEndpointUtils.concatEndpointAndPath(updatedEntity.getBaseEndpoint(), model.getAdapterCompletionEndpointPath()));
                        // Clear container and adapter completion endpoint path when removing from adapter
                        model.setModelContainer(null);
                        model.setAdapterCompletionEndpointPath(null);
                    });
            models.stream()
                    .filter(model -> !updatedEntity.getModels().contains(model))
                    .forEach(app -> {
                        // Clear container when setting adapter to ensure mutual exclusivity
                        app.setModelContainer(null);
                        app.setAdapter(updatedEntity);
                        app.setEndpoint(null);
                    });
            updatedEntity.getModels().clear();
            updatedEntity.getModels().addAll(models);
        }

        return updatedEntity;
    }

    @Mapping(target = "models", ignore = true)
    @Mapping(target = "adapterContainer", ignore = true)
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
