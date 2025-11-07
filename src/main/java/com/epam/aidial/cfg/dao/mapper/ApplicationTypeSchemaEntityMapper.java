package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
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

@Mapper(componentModel = "spring", uses = {PropertiesEntityMapper.class, DependentRouteEntityMapper.class})
public abstract class ApplicationTypeSchemaEntityMapper {

    @Autowired
    private ApplicationJpaRepository applicationJpaRepository;

    @Autowired
    private InterceptorJpaRepository interceptorJpaRepository;

    @Mapping(target = "applicationTypeRoutes", source = "routes")
    @Mapping(target = "applicationTypeInterceptors", source = "interceptors")
    public abstract ApplicationTypeSchema toDomain(ApplicationTypeSchemaEntity entity);

    protected String mapApplicationToString(ApplicationEntity value) {
        return value != null ? value.getDeploymentName() : null;
    }

    protected String mapInterceptorEntityToString(InterceptorEntity value) {
        return value != null ? value.getName() : null;
    }

    public ApplicationTypeSchemaEntity toEntity(ApplicationTypeSchema domain, ApplicationTypeSchemaEntity entity) {
        // todo: remove shouldUpdateApplications and related logic once FE is ready to send full state
        boolean shouldUpdateApplications = domain.getApplications() != null;
        List<ApplicationEntity> applications = shouldUpdateApplications
                ? findApplicationsByNames(domain.getApplications())
                : entity.getApplications();

        ApplicationTypeSchemaEntity updatedEntity = update(domain, entity);

        if (shouldUpdateApplications) {
            updatedEntity.getApplications().forEach(app -> {
                app.setApplicationTypeSchema(null);
                app.setEndpoint(updatedEntity.getSchemaId());
            });
            applications.forEach(app -> {
                app.setApplicationTypeSchema(updatedEntity);
                app.setEndpoint(null);
            });
            updatedEntity.getApplications().clear();
            updatedEntity.getApplications().addAll(applications);
        }

        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getApplicationTypeInterceptors());
        updatedEntity.setInterceptors(interceptors.isEmpty() ? null : interceptors);

        return updatedEntity;
    }

    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "routes", source = "applicationTypeRoutes")
    abstract ApplicationTypeSchemaEntity update(ApplicationTypeSchema domain, @MappingTarget ApplicationTypeSchemaEntity entity);

    private List<ApplicationEntity> findApplicationsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<ApplicationEntity> existingApplications = Lists.newArrayList(applicationJpaRepository.findAllById(names));
        Set<String> existingApplicationsNames = existingApplications.stream()
                .map(ApplicationEntity::getDeploymentName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingApplicationsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find applications: " + namesDiff);
        }

        return existingApplications;
    }

    private List<InterceptorEntity> findInterceptorsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<InterceptorEntity> existingInterceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptorsNames = existingInterceptors.stream()
                .map(InterceptorEntity::getName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptorsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find interceptors: " + namesDiff);
        }

        return existingInterceptors;
    }
}