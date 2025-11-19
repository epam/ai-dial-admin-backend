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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
        if (MapUtils.isEmpty(coreKeys)) {
            return Collections.emptyList();
        }

        AtomicInteger counter = new AtomicInteger();

        return coreKeys.entrySet().stream()
                .map(keyEntry -> processKey(keyEntry.getKey(), keyEntry.getValue(), resolutionPolicy, isPreview, counter))
                .toList();
    }

    public Collection<ImportComponent<Key>> importAdminKeys(Map<String, Key> keys,
                                                            ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(keys)) {
            return keys.entrySet().stream()
                    .map(keyEntry -> {
                                var key = keyEntry.getValue();
                                key.setName(keyEntry.getKey());
                                return processKey(key, resolutionPolicy);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<Key> processKey(Key key, ConflictResolutionPolicy resolutionPolicy) {
        Optional<Key> existingKey = keyService.tryGetKey(key.getName());
        if (existingKey.isPresent()) {
            ImportAction importAction = handleExisting(key, resolutionPolicy);
            return new ImportComponent<>(importAction, existingKey.get(), key);
        } else {
            ImportAction importAction = create(key);
            return new ImportComponent<>(importAction, null, key);
        }
    }

    private ImportComponent<Key> processKey(String keyValue,
                                            CoreKey coreKey,
                                            ConflictResolutionPolicy resolutionPolicy,
                                            boolean isPreview,
                                            AtomicInteger counter) {
        Optional<Key> existingKey = keyService.tryGetKeyByKeyValue(keyValue);
        if (existingKey.isPresent()) {
            Key existingKeyCopy = keyCoreMapper.copy(existingKey.get());
            Key key = keyCoreMapper.mapKey(coreKey, keyValue, existingKeyCopy);
            ImportAction importAction = handleExisting(key, resolutionPolicy);
            return new ImportComponent<>(importAction, existingKey.get(), key);
        } else {
            int i = counter.getAndIncrement();
            String name = isPreview ? "<will be defined during import " + i + ">" : UUID.randomUUID().toString();
            Key key = keyCoreMapper.mapKey(coreKey, keyValue, name);
            ImportAction importAction = create(key);
            return new ImportComponent<>(importAction, null, key);
        }
    }

    private ImportAction handleExisting(Key newKey, ConflictResolutionPolicy resolutionPolicy) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing key will remain unchanged.
            case OVERRIDE -> {
                keyService.updateKey(newKey.getName(), newKey);
                yield UPDATE;
            }
        };
    }

    private ImportAction create(Key key) {
        keyService.createKey(key);
        return CREATE;
    }

    public List<ImportComponent<Key>> getActualImportedKeys(Collection<ImportComponent<Key>> importComponents) {
        List<String> names = importComponents.stream()
                .map(ImportComponent::getNext)
                .map(Key::getName)
                .toList();
        Map<String, Key> importedKeysByNames = keyService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(Key::getName, Function.identity()));

        return importComponents.stream()
                .map(importComponent -> {
                    var next = importedKeysByNames.get(importComponent.getNext().getName());
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Key key) {
        if (key != null) {
            key.setCreatedAt(null);
            key.setUpdatedAt(null);
            key.setKeyGeneratedAt(null);
        }
    }
}
