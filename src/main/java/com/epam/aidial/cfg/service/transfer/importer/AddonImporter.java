package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.AddonCoreMapper;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
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
import java.util.Map;
import java.util.Optional;
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
                                                           ConfigImportOptions importOptions,
                                                           boolean isPreview) {
        if (MapUtils.isNotEmpty(coreAddons)) {
            Map<String, Addon> addons = coreAddons.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
            return importAdminAddons(addons, roles, importOptions, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Addon>> importAdminAddons(Map<String, Addon> addons,
                                                                Map<String, Role> roles,
                                                                ConfigImportOptions importOptions,
                                                                boolean isPreview) {
        if (MapUtils.isNotEmpty(addons)) {
            return addons.entrySet().stream()
                    .map(addonEntry -> {
                                var addon = addonEntry.getValue();
                                var importAction = processAddon(addonEntry.getKey(), addon, roles, importOptions.conflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, addon);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processAddon(String addonName,
                                      Addon newAddon,
                                      Map<String, Role> roles,
                                      ConflictResolutionPolicy resolutionPolicy,
                                      boolean isPreview) {
        Optional<Addon> addon = addonService.tryGetAddon(addonName);
        if (addon.isPresent()) {
            Addon existingAddon = addon.get();
            setLimits(addonName, existingAddon.getDeployment(), roles, newAddon.getDeployment(), isPreview);
            return handleExisting(newAddon, resolutionPolicy, addonName, isPreview);
        } else {
            setLimits(addonName, roles, newAddon.getDeployment(), isPreview);
            if (!isPreview) {
                addonService.createAddon(newAddon);
            }
            return CREATE;
        }
    }

    private ImportAction handleExisting(Addon newAddon,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String addonName,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing addon will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    addonService.updateAddon(addonName, newAddon);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private Addon map(String addonName, CoreAddon addon) {
        addon.setName(addonName);
        return addonCoreMapper.mapAddon(addon, new ShareResourceLimit());
    }
}
