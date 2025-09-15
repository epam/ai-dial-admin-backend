package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.AddonCoreMapper;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.domain.service.AddonService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreAddon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class AddonImporter extends RoleBasedImporter {

    private final AddonService addonService;
    private final AddonCoreMapper addonCoreMapper;

    public Collection<ImportComponent<Addon>> importAddons(Map<String, CoreAddon> coreAddons,
                                                           Map<String, Role> roles,
                                                           ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(coreAddons)) {
            Map<String, Addon> addons = coreAddons.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
            return importAdminAddons(addons, roles, importOptions);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Addon>> importAdminAddons(Map<String, Addon> addons,
                                                                Map<String, Role> roles,
                                                                ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(addons)) {
            return addons.entrySet().stream()
                    .map(addonEntry -> {
                                var addon = addonEntry.getValue();
                                return processAddon(addonEntry.getKey(), addon, roles, importOptions.conflictResolutionPolicy());
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<Addon> processAddon(String addonName,
                                                Addon newAddon,
                                                Map<String, Role> roles,
                                                ConflictResolutionPolicy resolutionPolicy) {
        Optional<Addon> addon = addonService.tryGetAddon(addonName);
        if (addon.isPresent()) {
            Addon existingAddon = addon.get();
            setLimits(addonName, existingAddon.getDeployment(), roles, newAddon.getDeployment());
            ImportAction importAction = handleExisting(newAddon, resolutionPolicy, addonName);
            return new ImportComponent<>(importAction, existingAddon, newAddon);
        } else {
            setLimits(addonName, roles, newAddon.getDeployment());
            addonService.createAddon(newAddon);
            return new ImportComponent<>(CREATE, null, newAddon);
        }
    }

    private ImportAction handleExisting(Addon newAddon,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String addonName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing addon will remain unchanged.
            case OVERRIDE -> {
                addonService.updateAddon(addonName, newAddon);
                yield UPDATE;
            }
        };
    }

    private Addon map(String addonName, CoreAddon addon) {
        addon.setName(addonName);
        return addonCoreMapper.mapAddon(addon, new ShareResourceLimit());
    }

    public List<ImportComponent<Addon>> getActualImportedAddons(Collection<ImportComponent<Addon>> addonImportComponents,
                                                                Collection<ImportComponent<Role>> roleImportComponents) {
        List<String> names = addonImportComponents.stream()
                .map(ImportComponent::getNext)
                .map(Addon::getDeployment)
                .map(Deployment::getName)
                .toList();
        Map<String, Addon> importedAddonsByNames = addonService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(addon -> addon.getDeployment().getName(), Function.identity()));

        Collection<Role> importedRoles = roleImportComponents.stream().map(ImportComponent::getNext).toList();
        List<RoleLimit> importedRoleLimits = importedRoles.stream().map(Role::getLimits).flatMap(Collection::stream).toList();
        List<RoleShareResourceLimit> importedRoleShareResourceLimits = importedRoles.stream().map(Role::getShare).flatMap(Collection::stream).toList();

        return addonImportComponents.stream()
                .map(importComponent -> {
                    var next = importedAddonsByNames.get(importComponent.getNext().getDeployment().getName());
                    setImportedLimits(next, importedRoleLimits, importedRoleShareResourceLimits);
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Addon addon) {
        if (addon != null) {
            addon.setCreatedAt(null);
            addon.setUpdatedAt(null);
        }
    }
}
