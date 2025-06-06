package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.KeyCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreKey;
import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class KeyImporter {

    private final KeyService keyService;
    private final KeyCoreMapper keyCoreMapper;

    public Collection<ImportComponent<Key>> importKeys(Map<String, CoreKey> coreKeys,
                                                       ConflictResolutionPolicy resolutionPolicy,
                                                       boolean isPreview) {
        if (MapUtils.isNotEmpty(coreKeys)) {
            Map<String, Key> keys = coreKeys.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, keyEntry -> map(keyEntry.getKey(), keyEntry.getValue())));
            return importAdminKeys(keys, resolutionPolicy, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Key>> importAdminKeys(Map<String, Key> keys,
                                                            ConflictResolutionPolicy resolutionPolicy,
                                                            boolean isPreview) {
        if (MapUtils.isNotEmpty(keys)) {
            return keys.entrySet().stream()
                    .map(keyEntry -> {
                                var key = keyEntry.getValue();
                                key.setName(keyEntry.getKey());
                                var importAction = processKey(keyEntry.getKey(), key, resolutionPolicy, isPreview);
                                return new ImportComponent<>(importAction, key);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processKey(String keyName, Key key, ConflictResolutionPolicy resolutionPolicy, boolean isPreview) {
        if (keyService.exists(keyName)) {
            return handleExisting(key, resolutionPolicy, keyName, isPreview);
        } else {
            return create(key, isPreview);
        }
    }

    private ImportAction handleExisting(Key newKey, ConflictResolutionPolicy resolutionPolicy, String keyName, boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing key will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    keyService.updateKey(keyName, newKey);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private ImportAction create(Key key, boolean isPreview) {
        if (!isPreview) {
            keyService.createKey(key);
        }
        return CREATE;
    }

    @VisibleForTesting
    protected Key map(String key, CoreKey coreKey) {
        coreKey.setKey(key);
        return keyCoreMapper.mapKey(coreKey);
    }
}
