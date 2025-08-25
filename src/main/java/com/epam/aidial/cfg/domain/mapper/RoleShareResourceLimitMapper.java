package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.core.config.CoreShareResourceLimit;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

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
public interface RoleShareResourceLimitMapper {

    ShareResourceLimit toLimit(CoreShareResourceLimit limit);

    default Map<String, CoreShareResourceLimit> mapShareResourceLimits(List<RoleShareResourceLimit> roleShareResourceLimits,
                                                                       @Context Collection<Deployment> deployments) {
        if (roleShareResourceLimits == null) {
            return Map.of();
        }

        Map<String, Deployment> deploymentsByName = CollectionUtils.emptyIfNull(deployments).stream()
                .collect(Collectors.toMap(Deployment::getName, Function.identity()));

        return roleShareResourceLimits.stream()
                .map(roleShareResourceLimit -> {
                    Deployment deployment = deploymentsByName.get(roleShareResourceLimit.getDeploymentName());
                    CoreShareResourceLimit mappedLimit = mapShareResourceLimit(roleShareResourceLimit.getLimit(), deployment);
                    return mappedLimit != null ? new AbstractMap.SimpleEntry<>(roleShareResourceLimit.getDeploymentName(), mappedLimit) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private CoreShareResourceLimit mapShareResourceLimit(ShareResourceLimit shareResourceLimit, Deployment deployment) {
        if (shareResourceLimit == null || deployment == null) {
            return null;
        }

        ShareResourceLimit defaultLimit = Optional.ofNullable(deployment.getDefaultRoleShareResourceLimit()).orElse(new ShareResourceLimit());
        if (isUnlimited(shareResourceLimit) && defaultLimit.isEmpty()) {
            return null;
        }

        CoreShareResourceLimit coreShareResourceLimit = new CoreShareResourceLimit();
        setIfNotNull(coreShareResourceLimit::setMaxAcceptedUsers, getIntLimitOrDefault(shareResourceLimit, defaultLimit, ShareResourceLimit::getMaxAcceptedUsers));
        setIfNotNull(coreShareResourceLimit::setInvitationTtl, getLongLimitOrDefault(shareResourceLimit, defaultLimit, ShareResourceLimit::getInvitationTtl));

        return coreShareResourceLimit;
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private Integer getIntLimitOrDefault(ShareResourceLimit limit, ShareResourceLimit defaultLimit, Function<ShareResourceLimit, Integer> limitValueGetter) {
        Integer limitValue = limitValueGetter.apply(limit);
        return isUnlimited(limitValue) ? limitValueGetter.apply(defaultLimit) : limitValue;
    }

    private Long getLongLimitOrDefault(ShareResourceLimit limit, ShareResourceLimit defaultLimit, Function<ShareResourceLimit, Long> limitValueGetter) {
        Long limitValue = limitValueGetter.apply(limit);
        return isUnlimited(limitValue) ? limitValueGetter.apply(defaultLimit) : limitValue;
    }

    private boolean isUnlimited(ShareResourceLimit shareResourceLimit) {
        return isUnlimited(shareResourceLimit.getInvitationTtl())
                && isUnlimited(shareResourceLimit.getMaxAcceptedUsers());
    }

    private boolean isUnlimited(Integer value) {
        return value == null || value == Integer.MAX_VALUE;
    }

    private boolean isUnlimited(Long value) {
        return value == null || value == Long.MAX_VALUE;
    }

}
