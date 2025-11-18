package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.model.ExportConfigComponent;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@LogExecution
@RequiredArgsConstructor
public class RoleExporter {

    private final RoleService roleService;

    protected Map<String, Role> getRoles(ExportRequest request, Set<String> allEntities) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.ROLE)
                    ? getRoles(allEntities).stream()
                    .collect(Collectors.toMap(Role::getName, Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getRoles(selectedItemsExportRequest.getComponents(), allEntities).stream()
                    .collect(Collectors.toMap(Role::getName, Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private Collection<Role> getRoles(Set<String> allEntities) {
        return getRoles().stream()
                .map(role -> removeDependency(role, allEntities))
                .toList();
    }

    protected Collection<Role> getRoles() {
        return roleService.getAllRoles();
    }

    private List<Role> getRoles(List<ExportConfigComponent> elements, Set<String> allEntities) {
        return elements.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.ROLE)
                .collect(Collectors.toMap(ExportConfigComponent::getName, Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }
                ))
                .values()
                .stream()
                .map(component -> {
                    Role role = roleService.getRole(component.getName());
                    return removeDependency(role, allEntities);
                })
                .toList();
    }

    protected Collection<ExportComponentInfo> preview(ExportRequest request) {
        return getRoles(request, Set.of()).values().stream()
                .map(component -> ExportComponentInfo.builder()
                        .name(component.getName())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.ROLE)
                        .build())
                .collect(Collectors.toList());
    }

    private Role removeDependency(Role role, Set<String> allEntities) {
        role.setKeys(null);

        if (CollectionUtils.isEmpty(allEntities)) {
            return role;
        }

        List<RoleLimit> limits = role.getLimits();
        if (CollectionUtils.isNotEmpty(limits)) {
            List<RoleLimit> filteredLimits = role.getLimits().stream()
                    .filter(limit -> allEntities.contains(limit.getDeploymentName()))
                    .collect(Collectors.toList());
            role.setLimits(filteredLimits);
        }

        return role;
    }

}
