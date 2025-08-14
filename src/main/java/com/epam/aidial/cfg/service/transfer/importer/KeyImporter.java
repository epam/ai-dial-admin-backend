package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.KeyCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
            return coreKeys.entrySet().stream()
                    .map(keyEntry -> {
                                var coreKey = keyEntry.getValue();
                                coreKey.setKey(keyEntry.getKey());
                                return processKey(coreKey, resolutionPolicy, isPreview);
                            }
                    )
                    .toList();
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
                                var importAction = processKey(key, resolutionPolicy, isPreview);
                                return new ImportComponent<>(importAction, key);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processKey(Key key, ConflictResolutionPolicy resolutionPolicy, boolean isPreview) {
        if (keyService.exists(key.getName())) {
            return handleExisting(key, resolutionPolicy, isPreview);
        } else {
            return create(key, isPreview);
        }
    }

    private ImportComponent<Key> processKey(CoreKey coreKey, ConflictResolutionPolicy resolutionPolicy, boolean isPreview) {
        Optional<Key> existingKey = keyService.tryGetKeyByKeyValue(coreKey.getKey());
        if (existingKey.isPresent()) {
            Key key = keyCoreMapper.mapKey(coreKey, existingKey.get().getName());
            ImportAction importAction = handleExisting(key, resolutionPolicy, isPreview);
            return new ImportComponent<>(importAction, key);
        } else {
            String name = isPreview ? "<will be defined during import>" : UUID.randomUUID().toString();
            Key key = keyCoreMapper.mapKey(coreKey, name);
            ImportAction importAction = create(key, isPreview);
            return new ImportComponent<>(importAction, key);
        }
    }

    private ImportAction handleExisting(Key newKey, ConflictResolutionPolicy resolutionPolicy, boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing key will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    keyService.updateKey(newKey.getName(), newKey);
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
}
