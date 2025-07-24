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
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = RoleLimitMapper.class)
public interface RoleCoreMapper {

    CoreRole mapRole(Role role, Collection<Deployment> deployments);

    @Mapping(target = "keys", ignore = true)
    @Mapping(target = "limits", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "share", ignore = true)
    Role mapToRole(CoreRole role,
                   Map<String, CoreLimit> limits,
                   Map<String, CoreShareResourceLimit> shareResourceLimits,
                   List<RoleLimit> roleLimitsToMergeWithCoreLimits,
                   List<RoleShareResourceLimit> roleShareResourceLimitsToMergeWithCoreShareResourceLimits,
                   Map<String, Set<String>> userRolesByDeploymentName);

    @AfterMapping
    default void afterMapping(@MappingTarget Role role,
                              Map<String, CoreLimit> limits,
                              Map<String, CoreShareResourceLimit> shareResourceLimits,
                              List<RoleLimit> roleLimitsToMergeWithCoreLimits,
                              List<RoleShareResourceLimit> roleShareResourceLimitsToMergeWithCoreShareResourceLimits,
                              Map<String, Set<String>> userRolesByDeploymentName) {
        List<RoleLimit> coreRoleLimits = map(role.getName(), limits, userRolesByDeploymentName);
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
    RoleLimit toLimit(CoreLimit limit, String deploymentName, boolean enabled);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "limit.maxAcceptedUsers", source = "maxAcceptedUsers")
    @Mapping(target = "limit.invitationTtl", source = "invitationTtl")
    RoleShareResourceLimit toShareResourceLimit(CoreShareResourceLimit shareResourceLimit);

    default List<RoleLimit> map(String roleName, Map<String, CoreLimit> value, Map<String, Set<String>> userRolesByDeploymentName) {
        if (MapUtils.isEmpty(value)) {
            return null;
        }

        List<RoleLimit> roleLimits = value.entrySet()
                .stream()
                .map(e -> {
                    String deploymentName = e.getKey();
                    Set<String> userRoles = userRolesByDeploymentName.get(deploymentName);
                    boolean enabled = SetUtils.emptyIfNull(userRoles).contains(roleName);
                    return toLimit(e.getValue(), deploymentName, enabled);
                })
                .toList();
        Set<String> deploymentNamesOfAlreadyAddedRoleLimits = roleLimits.stream()
                .map(RoleLimit::getDeploymentName)
                .collect(Collectors.toSet());

        List<RoleLimit> userRoleLimits = userRolesByDeploymentName.entrySet().stream()
                .filter(entry -> !deploymentNamesOfAlreadyAddedRoleLimits.contains(entry.getKey()) && entry.getValue().contains(roleName))
                .map(entry -> toLimit(new CoreLimit(), entry.getKey(), true))
                .toList();

        return ListUtils.union(roleLimits, userRoleLimits);
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
