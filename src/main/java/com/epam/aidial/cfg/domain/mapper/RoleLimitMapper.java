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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.utils.NullSafeUtils.getValueOrDefault;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class RoleLimitMapper {
    public abstract Limit toLimit(CoreLimit limit);

    @Named("mapToCoreLimits")
    public Map<String, CoreLimit> mapLimits(List<RoleLimit> roleLimits, @Context Collection<Deployment> deployments) {
        Map<String, CoreLimit> result = new HashMap<>();

        CollectionUtils.emptyIfNull(deployments).stream()
                .filter(Deployment::getIsPublic)
                .filter(deployment -> !deployment.getDefaultRoleLimit().isEmpty())
                .forEach(deployment -> {
                    CoreLimit mappedLimit = mapLimit(new Limit(), deployment);
                    result.put(deployment.getName(), mappedLimit);
                });

        Map<String, Deployment> deploymentsByName = CollectionUtils.emptyIfNull(deployments).stream()
                .collect(Collectors.toMap(Deployment::getName, Function.identity()));

        CollectionUtils.emptyIfNull(roleLimits)
                .forEach(roleLimit -> {
                    Deployment deployment = deploymentsByName.get(roleLimit.getDeploymentName());
                    CoreLimit mappedLimit = mapLimit(roleLimit.getLimit(), deployment);

                    if (!mappedLimit.isEmpty()) {
                        mappedLimit = mappedLimit.isUnlimited() ? CoreLimit.empty() : mappedLimit;
                        result.put(roleLimit.getDeploymentName(), mappedLimit);
                    }
                });

        return result;
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

        coreLimit.setMinute(getValueOrDefault(limit, defaultLimit, Limit::getMinute));
        coreLimit.setDay(getValueOrDefault(limit, defaultLimit, Limit::getDay));
        coreLimit.setWeek(getValueOrDefault(limit, defaultLimit, Limit::getWeek));
        coreLimit.setMonth(getValueOrDefault(limit, defaultLimit, Limit::getMonth));
        coreLimit.setRequestHour(getValueOrDefault(limit, defaultLimit, Limit::getRequestHour));
        coreLimit.setRequestDay(getValueOrDefault(limit, defaultLimit, Limit::getRequestDay));

        return coreLimit;
    }
}
