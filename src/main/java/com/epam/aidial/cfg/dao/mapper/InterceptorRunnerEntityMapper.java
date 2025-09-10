package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
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

@Mapper(componentModel = "spring", uses = PropertiesEntityMapper.class)
public abstract class InterceptorRunnerEntityMapper {

    @Autowired
    private InterceptorJpaRepository interceptorJpaRepository;

    public abstract InterceptorRunner toDomain(InterceptorRunnerEntity entity);

    protected String mapInterceptorToString(InterceptorEntity value) {
        return value != null ? value.getId() : null;
    }

    public InterceptorRunnerEntity toEntity(InterceptorRunner domain, InterceptorRunnerEntity entity) {
        boolean shouldUpdateInterceptors = domain.getInterceptors() != null;
        List<InterceptorEntity> interceptors = shouldUpdateInterceptors
                ? findInterceptorsByNames(domain.getInterceptors())
                : entity.getInterceptors();

        InterceptorRunnerEntity updatedEntity = update(domain, entity);

        if (shouldUpdateInterceptors) {
            updatedEntity.getInterceptors().forEach(interceptor -> {
                interceptor.setInterceptorRunner(null);
                interceptor.setEndpoint(updatedEntity.getCompletionEndpoint());
                if (interceptor.getFeatures() == null) {
                    interceptor.setFeatures(new FeaturesEntity());
                }
                interceptor.getFeatures().setConfigurationEndpoint(updatedEntity.getConfigurationEndpoint());
            });
            interceptors.forEach(interceptor -> {
                interceptor.setInterceptorRunner(updatedEntity);
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

    private List<InterceptorEntity> findInterceptorsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<InterceptorEntity> existingInterceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptorsNames = existingInterceptors.stream()
                .map(InterceptorEntity::getId)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptorsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find Interceptors: " + namesDiff);
        }

        return existingInterceptors;
    }
}