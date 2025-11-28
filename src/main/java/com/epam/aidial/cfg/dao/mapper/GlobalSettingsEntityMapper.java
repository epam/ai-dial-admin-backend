package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.GlobalSettingsEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
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
public abstract class GlobalSettingsEntityMapper {
    @Autowired
    private InterceptorJpaRepository interceptorJpaRepository;

    public GlobalSettingsEntity toGlobalSettingsEntity(GlobalSettings globalSettings, GlobalSettingsEntity entity) {
        validateGlobalInterceptorsByNames(globalSettings.getGlobalInterceptors());
        return update(globalSettings, entity);
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", constant = "1")
    public abstract GlobalSettingsEntity update(GlobalSettings globalSettings, @MappingTarget GlobalSettingsEntity entity);

    public abstract GlobalSettings toDomain(GlobalSettingsEntity globalSettingsEntity);

    private void validateGlobalInterceptorsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return;
        }

        List<InterceptorEntity> interceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptors = interceptors.stream().map(InterceptorEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptors);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("One or more global interceptor IDs do not exist as interceptors: " + namesDiff);
        }
    }
}