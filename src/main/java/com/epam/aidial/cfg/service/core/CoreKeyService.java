package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.KeyCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
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

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service
@RequiredArgsConstructor
@Slf4j
public class CoreKeyService {

    private final KeyService keyService;
    private final KeyCoreMapper keyCoreMapper;
    private final ConfigImporter configImporter;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreKey> getCoreKeyWithHash(String keyName) {
        var keyWithHash = keyService.getKeyWithHash(keyName);
        var coreKey = keyCoreMapper.mapKey(keyWithHash.model());
        return new CoreWithDomainHash<>(coreKey, keyWithHash.hash());
    }

    @Transactional
    public String updateKey(String keyName, CoreKey coreKey, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Key:%s.", keyName));
        }

        var keyWithHash = keyService.getKeyWithHash(keyName);

        assertNotConcurrencyOverwrite(keyWithHash, hash);
        importCoreKey(keyWithHash.model().getKey(), coreKey);

        return keyService.getKeyWithHash(keyName).hash();
    }

    private void assertNotConcurrencyOverwrite(DomainObjectWithHash<Key> keyWithHash, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        Key key = keyWithHash.model();
        String currentHash = keyWithHash.hash();

        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: keyName={}, expectedHash={}, currentHash={}",
                    key.getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Unable to update Key '%s'. The data may have been modified by another user, "
                            + "or the name/ID may already exist. Please reload the data and try again.",
                    key.getName()));
        }
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
}
