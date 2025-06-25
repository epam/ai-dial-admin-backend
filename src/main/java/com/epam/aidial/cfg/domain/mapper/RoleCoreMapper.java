package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreShareResourceLimit;
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
import java.util.function.Function;
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
    @Mapping(target = "share", ignore = true)
    Role mapToRole(CoreRole role,
                   Map<String, CoreLimit> limits,
                   Map<String, CoreShareResourceLimit> shareResourceLimits,
                   List<RoleLimit> roleLimitsToMergeWithCoreLimits,
                   List<RoleShareResourceLimit> existingShareResourceLimitsNotPresentInConfig);

    @AfterMapping
    default void afterMapping(@MappingTarget Role role,
                              CoreRole coreRole,
                              Map<String, CoreLimit> limits,
                              Map<String, CoreShareResourceLimit> shareResourceLimits,
                              List<RoleLimit> roleLimitsToMergeWithCoreLimits,
                              List<RoleShareResourceLimit> roleShareResourceLimitsToMergeWithCoreShareResourceLimits) {
        if (!Objects.equals(coreRole.getName(), DEFAULT_ROLE_NAME)) {
            List<RoleLimit> coreRoleLimits = map(limits);
            List<RoleShareResourceLimit> coreRoleShareResourceLimits = mapShareResourceLimitsToList(shareResourceLimits);

            if (coreRoleLimits == null && CollectionUtils.isEmpty(roleLimitsToMergeWithCoreLimits)) {
                role.setLimits(null);
            } else {
                role.setLimits(mergeLimits(coreRoleLimits, roleLimitsToMergeWithCoreLimits));
            }

            if (coreRoleShareResourceLimits == null && CollectionUtils.isEmpty(roleShareResourceLimitsToMergeWithCoreShareResourceLimits)) {
                role.setShare(null);
            } else {
                role.setShare(mergeShareResourceLimits(coreRoleShareResourceLimits, roleShareResourceLimitsToMergeWithCoreShareResourceLimits));
            }
        }
    }

    private List<RoleLimit> mergeLimits(List<RoleLimit> coreRoleLimits, List<RoleLimit> roleLimitsToMergeWithCoreLimits) {
        Map<String, RoleLimit> roleLimitsByDeploymentName = CollectionUtils.emptyIfNull(coreRoleLimits).stream()
                .collect(Collectors.toMap(RoleLimit::getDeploymentName, Function.identity()));

        CollectionUtils.emptyIfNull(roleLimitsToMergeWithCoreLimits)
                .forEach(roleLimit -> roleLimitsByDeploymentName.putIfAbsent(roleLimit.getDeploymentName(), roleLimit));

        return roleLimitsByDeploymentName.values().stream().toList();
    }

    private List<RoleShareResourceLimit> mergeShareResourceLimits(List<RoleShareResourceLimit> coreRoleShareResourceLimits,
                                                                  List<RoleShareResourceLimit> roleShareResourceLimitsToMergeWithCoreShareResourceLimits) {
        Map<String, RoleShareResourceLimit> limitsByDeploymentName = CollectionUtils.emptyIfNull(coreRoleShareResourceLimits).stream()
                .collect(Collectors.toMap(RoleShareResourceLimit::getDeploymentName, Function.identity()));

        CollectionUtils.emptyIfNull(roleShareResourceLimitsToMergeWithCoreShareResourceLimits)
                .forEach(limit -> limitsByDeploymentName.putIfAbsent(limit.getDeploymentName(), limit));

        return limitsByDeploymentName.values().stream().toList();
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

    default Map<String, CoreShareResourceLimit> mapShareResourceLimits(List<RoleShareResourceLimit> shareResourceLimits) {
        if (shareResourceLimits == null) {
            return Map.of();
        }

        return shareResourceLimits.stream()
                .map(shareResourceLimit -> {
                    CoreShareResourceLimit mappedLimit = mapShareResourceLimit(shareResourceLimit.getLimit());
                    return mappedLimit != null ? new AbstractMap.SimpleEntry<>(shareResourceLimit.getDeploymentName(), mappedLimit) : null;
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

    default CoreShareResourceLimit mapShareResourceLimit(ShareResourceLimit shareResourceLimit) {
        if (shareResourceLimit == null) {
            return null;
        }

        CoreShareResourceLimit coreShareResourceLimit = new CoreShareResourceLimit();

        var invitationTtl = shareResourceLimit.getInvitationTtl();
        if (invitationTtl != null) {
            coreShareResourceLimit.setInvitationTtl(invitationTtl);
        }
        var maxAcceptedUsers = shareResourceLimit.getMaxAcceptedUsers();
        if (maxAcceptedUsers != null) {
            coreShareResourceLimit.setMaxAcceptedUsers(maxAcceptedUsers);
        }

        return coreShareResourceLimit;
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

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "limit.maxAcceptedUsers", source = "maxAcceptedUsers")
    @Mapping(target = "limit.invitationTtl", source = "invitationTtl")
    RoleShareResourceLimit toShareResourceLimit(CoreShareResourceLimit shareResourceLimit);

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

    default List<RoleShareResourceLimit> mapShareResourceLimitsToList(Map<String, CoreShareResourceLimit> value) {
        if (MapUtils.isEmpty(value)) {
            return null;
        }
        return value.entrySet()
                .stream()
                .map(e -> {
                    RoleShareResourceLimit shareLimit = toShareResourceLimit(e.getValue());
                    shareLimit.setDeploymentName(e.getKey());
                    return shareLimit;
                })
                .collect(Collectors.toList());
    }

}
