package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.core.config.CoreShareResourceLimit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class RoleShareResourceLimitMapper extends LimitMapper {

    public abstract ShareResourceLimit toLimit(CoreShareResourceLimit limit);

    public Map<String, CoreShareResourceLimit> mapShareResourceLimits(List<RoleShareResourceLimit> roleShareResourceLimits,
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
                    return new AbstractMap.SimpleEntry<>(roleShareResourceLimit.getDeploymentName(), mappedLimit);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private CoreShareResourceLimit mapShareResourceLimit(ShareResourceLimit shareResourceLimit, Deployment deployment) {
        if (shareResourceLimit == null || deployment == null) {
            log.warn("Share resource limit or deployment is null. ShareResourceLimit: {}, deployment: {}", shareResourceLimit, deployment);
            throw new IllegalStateException("Share resource limit or deployment is null");
        }

        ShareResourceLimit defaultLimit = deployment.getDefaultRoleShareResourceLimit();
        if (defaultLimit == null) {
            log.warn("Deployment default share resource limit is null. Deployment: {}", deployment);
            throw new IllegalStateException("Deployment default share resource limit is null");
        }

        CoreShareResourceLimit coreShareResourceLimit = new CoreShareResourceLimit();

        setIfNotNull(coreShareResourceLimit::setMaxAcceptedUsers, getLimitOrDefault(shareResourceLimit, defaultLimit, ShareResourceLimit::getMaxAcceptedUsers));
        setIfNotNull(coreShareResourceLimit::setInvitationTtl, getLimitOrDefault(shareResourceLimit, defaultLimit, ShareResourceLimit::getInvitationTtl));

        return coreShareResourceLimit;
    }

}
