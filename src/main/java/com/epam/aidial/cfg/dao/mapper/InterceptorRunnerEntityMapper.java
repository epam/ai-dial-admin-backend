package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = PropertiesEntityMapper.class)
public abstract class InterceptorRunnerEntityMapper {

    public abstract InterceptorRunner toDomain(InterceptorRunnerEntity entity);

    protected String mapInterceptorToString(InterceptorEntity value) {
        return value != null ? value.getId() : null;
    }

    public InterceptorRunnerEntity toEntity(InterceptorRunner domain,
                                            InterceptorRunnerEntity entity,
                                            List<InterceptorEntity> interceptors) {
        InterceptorRunnerEntity updatedEntity = update(domain, entity);

        boolean shouldUpdateInterceptors = interceptors != null;
        if (shouldUpdateInterceptors) {
            updatedEntity.getInterceptors().stream()
                    .filter(interceptor -> !interceptors.contains(interceptor))
                    .forEach(interceptor -> {
                        interceptor.setInterceptorRunner(null);
                        interceptor.setInterceptorContainer(null);
                        interceptor.setEndpoint(updatedEntity.getCompletionEndpoint());
                        if (interceptor.getFeatures() == null) {
                            interceptor.setFeatures(new FeaturesEntity());
                        }
                        interceptor.getFeatures().setConfigurationEndpoint(updatedEntity.getConfigurationEndpoint());
                    });
            interceptors.stream()
                    .filter(interceptor -> !updatedEntity.getInterceptors().contains(interceptor))
                    .forEach(interceptor -> {
                        interceptor.setInterceptorRunner(updatedEntity);
                        interceptor.setInterceptorContainer(null);
                        interceptor.setEndpoint(null);
                        if (interceptor.getFeatures() != null) {
                            interceptor.getFeatures().setConfigurationEndpoint(null);
                        }
                    });
            updatedEntity.getInterceptors().clear();
            updatedEntity.getInterceptors().addAll(interceptors);
        }

        return updatedEntity;
    }

    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    abstract InterceptorRunnerEntity update(InterceptorRunner domain, @MappingTarget InterceptorRunnerEntity entity);
}