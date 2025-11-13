package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.AddonCoreMapper;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.RoleLimit;
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
public class AddonImporter extends DeploymentHolderImporter {

    private final AddonService addonService;
    private final AddonCoreMapper addonCoreMapper;

    public Collection<ImportComponent<Addon>> importAddons(Map<String, CoreAddon> coreAddons,
                                                           ConfigImportOptions importOptions) {
        if (MapUtils.isEmpty(coreAddons)) {
            return Collections.emptyList();
        }

        return coreAddons.entrySet()
                .stream()
                .map(entry -> processAddon(entry.getKey(), entry.getValue(), importOptions.conflictResolutionPolicy()))
                .toList();
    }

    private ImportComponent<Addon> processAddon(String addonName,
                                                CoreAddon coreAddon,
                                                ConflictResolutionPolicy resolutionPolicy) {
        Optional<Addon> addon = addonService.tryGetAddon(addonName);
        if (addon.isPresent()) {
            Addon existingAddon = addon.get();
            Addon existingAddonCopy = addonCoreMapper.copy(existingAddon);
            List<RoleLimit> roleLimits = getRoleLimits(existingAddonCopy.getDeployment(), coreAddon.getUserRoles());
            Addon newAddon = map(addonName, coreAddon, roleLimits, existingAddonCopy);
            ImportAction importAction = handleExisting(newAddon, resolutionPolicy, addonName);
            return new ImportComponent<>(importAction, existingAddon, newAddon);
        } else {
            List<RoleLimit> roleLimits = getRoleLimits(addonName, coreAddon.getUserRoles());
            Addon newAddon = map(addonName, coreAddon, roleLimits, new Addon());
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

    private Addon map(String addonName, CoreAddon coreAddon, List<RoleLimit> roleLimits, Addon addon) {
        coreAddon.setName(addonName);
        return addonCoreMapper.mapAddon(coreAddon, roleLimits, addon);
    }

    public List<ImportComponent<Addon>> getActualImportedAddons(Collection<ImportComponent<Addon>> addonImportComponents) {
        List<String> names = getNextImportComponentNames(addonImportComponents);
        Map<String, Addon> importedAddonsByNames = addonService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(addon -> addon.getDeployment().getName(), Function.identity()));

        return addonImportComponents.stream()
                .map(importComponent -> {
                    var next = importedAddonsByNames.get(importComponent.getNext().getDeployment().getName());
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
