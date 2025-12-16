package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.KeyCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException.OptimisticLockConflictExceptionDetails;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateResolver;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service
@RequiredArgsConstructor
@Slf4j
public class CoreKeyService {

    private final KeyService keyService;
    private final KeyCoreMapper keyCoreMapper;
    private final ConfigImporter configImporter;
    private final EntitySyncStateResolver entitySyncStateResolver;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreKey> getCoreKeyWithHash(String keyName) {
        var keyWithHash = keyService.getKeyWithHash(keyName);
        var coreKey = keyCoreMapper.mapKey(keyWithHash.model());
        return new CoreWithDomainHash<>(coreKey, keyWithHash.hash());
    }

    @Transactional
    public String updateKey(String keyName, CoreKey coreKey, String hash) {
        assertHashNotNull(keyName, hash);

        var keyWithHash = keyService.getKeyWithHash(keyName);

        assertKeyWasNotUpdated(keyWithHash, hash, OptimisticLockConflictException::onUpdate);
        importCoreKey(keyWithHash.model().getKey(), coreKey);

        return keyService.getKeyWithHash(keyName).hash();
    }

    private void importCoreKey(String key, CoreKey coreKey) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Current key can not be updated since it doesn't have key value.");
        }

        Map<String, CoreKey> coreKeys = new HashMap<>(1);
        coreKeys.put(key, coreKey);

        Config config = new Config();
        config.setKeys(coreKeys);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String keyName, String hash) {
        assertHashNotNull(keyName, hash);

        var keyWithHash = keyService.getKeyWithHash(keyName);
        assertKeyWasNotUpdated(keyWithHash, hash, OptimisticLockConflictException::onGetSyncState);

        var key = keyWithHash.model();
        if (StringUtils.isBlank(key.getKey())) {
            return EntitySyncState.unknown();
        }

        var coreKey = keyCoreMapper.mapKey(key);
        boolean isKeyValid = key.getValidityState().isValid();

        return entitySyncStateResolver.resolveForEntityInObject(coreKey, isKeyValid, key.getUpdatedAt(), "keys", key.getKey());
    }

    private void assertHashNotNull(String keyName, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    String.format("Hash must not be null. Use \"*\" to skip optimistic check. Key:%s.", keyName)
            );
        }
    }

    private void assertKeyWasNotUpdated(DomainObjectWithHash<Key> keyWithHash,
                                        String expectedHash,
                                        Function<OptimisticLockConflictExceptionDetails, OptimisticLockConflictException> exceptionProvider) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        String currentHash = keyWithHash.hash();
        if (!expectedHash.equals(currentHash)) {
            String keyName = keyWithHash.model().getName();
            throw exceptionProvider.apply(new OptimisticLockConflictExceptionDetails("Key", keyName, expectedHash, currentHash));
        }
    }
}
