package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.AddonCoreMapper;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.AddonService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreAddon;
import com.epam.aidial.core.config.CoreRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
public class AddonImporter extends RoleBasedImporter {

    private final AddonService addonService;
    private final AddonCoreMapper addonCoreMapper;

    public AddonImporter(RoleService roleService, AddonService addonService, AddonCoreMapper addonCoreMapper) {
        super(roleService);
        this.addonService = addonService;
        this.addonCoreMapper = addonCoreMapper;
    }

    public Collection<ImportComponent<Addon>> importAddons(Map<String, CoreAddon> coreAddons,
                                                           Map<String, CoreRole> roles,
                                                           ConfigImportOptions importOptions,
                                                           boolean isPreview) {
        if (MapUtils.isNotEmpty(coreAddons)) {
            Map<String, Addon> addons = coreAddons.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue(), roles)));
            return importAdminAddons(addons, importOptions, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Addon>> importAdminAddons(Map<String, Addon> addons,
                                                                ConfigImportOptions importOptions,
                                                                boolean isPreview) {
        if (MapUtils.isNotEmpty(addons)) {
            return addons.entrySet().stream()
                    .map(addonEntry -> {
                                var addon = addonEntry.getValue();
                                createRoleIfAbsent(importOptions, addon.getDeployment().getRoleLimits());
                                var importAction = processAddon(addonEntry.getKey(), addon, importOptions.getConflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, addon);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processAddon(String addonName,
                                      Addon newAddon,
                                      ConflictResolutionPolicy resolutionPolicy,
                                      boolean isPreview) {
        if (addonService.exists(addonName)) {
            return handleExisting(newAddon, resolutionPolicy, addonName, isPreview);
        } else {
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

    private Addon map(String addonName, CoreAddon addon, Map<String, CoreRole> roles) {
        addon.setName(addonName);
        return addonCoreMapper.mapAddon(addon, roles);
    }
}
