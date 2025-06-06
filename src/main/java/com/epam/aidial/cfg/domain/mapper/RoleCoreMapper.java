package com.epam.aidial.cfg.domain.mapper;


import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.epam.aidial.core.config.CoreRole.DEFAULT_ROLE_NAME;

@Mapper(componentModel = "spring")
public interface RoleCoreMapper {

    CoreRole mapRole(Role role, Collection<Deployment> deployments);

    @AfterMapping
    default void mapDefaultLimits(@MappingTarget CoreRole coreRole, Role role, Collection<Deployment> deployments) {
        if (!Objects.equals(role.getName(), CoreRole.DEFAULT_ROLE_NAME) || CollectionUtils.isEmpty(deployments)) {
            return;
        }
        Map<String, CoreLimit> limits = deployments.stream()
                .filter(d -> !isUnlimited(d.getDefaultRoleLimit()))
                .collect(Collectors.toMap(Deployment::getName, d -> mapLimit(d.getDefaultRoleLimit())
                ));
        coreRole.setLimits(limits);
    }

    @Mapping(target = "keys", ignore = true)
    @Mapping(target = "limits", ignore = true)
    @Mapping(target = "description", ignore = true)
    Role mapToRole(CoreRole role, Map<String, CoreLimit> limits);

    @AfterMapping
    default void afterMapping(@MappingTarget Role role, CoreRole coreRole, Map<String, CoreLimit> limits) {
        if (!Objects.equals(coreRole.getName(), DEFAULT_ROLE_NAME)) {
            role.setLimits(map(limits));
        }
    }

    default Map<String, CoreLimit> mapLimits(List<RoleLimit> roleLimits) {
        if (roleLimits == null) {
            return Map.of();
        }

        return roleLimits.stream()
                .map(roleLimit -> {
                    CoreLimit mappedLimit = mapLimit(roleLimit.getLimit());
                    return mappedLimit != null ? new AbstractMap.SimpleEntry<>(roleLimit.getDeploymentName(), mappedLimit) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    default CoreLimit mapLimit(Limit limit) {
        if (limit == null || isUnlimited(limit)) {
            return null;
        }
        CoreLimit coreLimit = new CoreLimit();
        setIfNotNull(coreLimit::setMinute, limit.getMinute());
        setIfNotNull(coreLimit::setDay, limit.getDay());
        setIfNotNull(coreLimit::setWeek, limit.getWeek());
        setIfNotNull(coreLimit::setMonth, limit.getMonth());
        setIfNotNull(coreLimit::setRequestHour, limit.getRequestHour());
        setIfNotNull(coreLimit::setRequestDay, limit.getRequestDay());
        return coreLimit;
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private boolean isUnlimited(Limit limit) {
        return (limit.getMinute() == null || limit.getMinute() == Long.MAX_VALUE)
                && (limit.getDay() == null || limit.getDay() == Long.MAX_VALUE)
                && (limit.getWeek() == null || limit.getWeek() == Long.MAX_VALUE)
                && (limit.getMonth() == null || limit.getMonth() == Long.MAX_VALUE)
                && (limit.getRequestHour() == null || limit.getRequestHour() == Long.MAX_VALUE)
                && (limit.getRequestDay() == null || limit.getRequestDay() == Long.MAX_VALUE);
    }

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "limit.minute", source = "minute")
    @Mapping(target = "limit.day", source = "day")
    @Mapping(target = "limit.week", source = "week")
    @Mapping(target = "limit.month", source = "month")
    @Mapping(target = "limit.requestHour", source = "requestHour")
    @Mapping(target = "limit.requestDay", source = "requestDay")
    RoleLimit toLimit(CoreLimit limit);

    default List<RoleLimit> map(Map<String, CoreLimit> value) {
        if (MapUtils.isEmpty(value)) {
            return null;
        }
        return value.entrySet()
                .stream()
                .map(e -> {
                    RoleLimit roleLimit = toLimit(e.getValue());
                    roleLimit.setDeploymentName(e.getKey());
                    return roleLimit;
                })
                .collect(Collectors.toList());
    }

}
