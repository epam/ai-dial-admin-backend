package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreLimit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class RoleLimitMapper extends LimitMapper {
    public abstract Limit toLimit(CoreLimit limit);

    Long getLimit(long value) {
        return Long.MAX_VALUE == value ? null : value;
    }

    @Named("mapToCoreLimits")
    public Map<String, CoreLimit> mapLimits(List<RoleLimit> roleLimits, @Context Collection<Deployment> deployments) {
        if (roleLimits == null) {
            return Map.of();
        }

        Map<String, Deployment> deploymentsByName = CollectionUtils.emptyIfNull(deployments).stream()
                .collect(Collectors.toMap(Deployment::getName, Function.identity()));

        return roleLimits.stream()
                .map(roleLimit -> {
                    Deployment deployment = deploymentsByName.get(roleLimit.getDeploymentName());
                    CoreLimit mappedLimit = mapLimit(roleLimit.getLimit(), deployment);
                    return new AbstractMap.SimpleEntry<>(roleLimit.getDeploymentName(), mappedLimit);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private CoreLimit mapLimit(Limit limit, Deployment deployment) {
        if (limit == null || deployment == null) {
            log.warn("Limit or deployment is null. Limit: {}, deployment: {}", limit, deployment);
            throw new IllegalStateException("Limit or deployment is null");
        }

        Limit defaultLimit = deployment.getDefaultRoleLimit();
        if (defaultLimit == null) {
            log.warn("Deployment default limit is null. Deployment: {}", deployment);
            throw new IllegalStateException("Deployment default limit is null");
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

}
