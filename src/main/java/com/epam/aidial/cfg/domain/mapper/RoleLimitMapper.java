package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreLimit;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleLimitMapper {
    Limit toLimit(CoreLimit limit);

    default Long getLimit(long value) {
        return Long.MAX_VALUE == value ? null : value;
    }

    @Named("mapToCoreLimits")
    default Map<String, CoreLimit> mapLimits(List<RoleLimit> roleLimits, @Context Collection<Deployment> deployments) {
        if (roleLimits == null) {
            return Map.of();
        }

        Map<String, Deployment> deploymentsByName = CollectionUtils.emptyIfNull(deployments).stream()
                .collect(Collectors.toMap(Deployment::getName, Function.identity()));

        return roleLimits.stream()
                .map(roleLimit -> {
                    Deployment deployment = deploymentsByName.get(roleLimit.getDeploymentName());
                    CoreLimit mappedLimit = mapLimit(roleLimit.getLimit(), deployment);
                    return mappedLimit != null ? new AbstractMap.SimpleEntry<>(roleLimit.getDeploymentName(), mappedLimit) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private CoreLimit mapLimit(Limit limit, Deployment deployment) {
        if (limit == null || deployment == null) {
            return null;
        }

        Limit defaultLimit = Optional.ofNullable(deployment.getDefaultRoleLimit()).orElse(new Limit());
        if (isUnlimited(limit) && defaultLimit.isEmpty()) {
            return null;
        }

        CoreLimit coreLimit = new CoreLimit();
        setIfNotNull(coreLimit::setMinute, getLimitOrDefault(limit, defaultLimit, Limit::getMinute));
        setIfNotNull(coreLimit::setDay, getLimitOrDefault(limit, defaultLimit, Limit::getDay));
        setIfNotNull(coreLimit::setWeek, getLimitOrDefault(limit, defaultLimit, Limit::getWeek));
        setIfNotNull(coreLimit::setMonth, getLimitOrDefault(limit, defaultLimit, Limit::getMonth));
        setIfNotNull(coreLimit::setRequestHour, getLimitOrDefault(limit, defaultLimit, Limit::getRequestHour));
        setIfNotNull(coreLimit::setRequestDay, getLimitOrDefault(limit, defaultLimit, Limit::getRequestDay));
        return coreLimit;
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private Long getLimitOrDefault(Limit limit, Limit defaultLimit, Function<Limit, Long> limitValueGetter) {
        Long limitValue = limitValueGetter.apply(limit);
        return isUnlimited(limitValue) ? limitValueGetter.apply(defaultLimit) : limitValue;
    }

    private boolean isUnlimited(Limit limit) {
        return isUnlimited(limit.getMinute())
                && isUnlimited(limit.getDay())
                && isUnlimited(limit.getWeek())
                && isUnlimited(limit.getMonth())
                && isUnlimited(limit.getRequestHour())
                && isUnlimited(limit.getRequestDay());
    }

    private boolean isUnlimited(Long value) {
        return value == null || value == Long.MAX_VALUE;
    }

}
