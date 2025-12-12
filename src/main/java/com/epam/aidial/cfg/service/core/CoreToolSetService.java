package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ToolSetCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException.OptimisticLockConflictExceptionDetails;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateResolver;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreToolSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CoreToolSetService {

    private final ToolSetService toolSetService;
    private final ToolSetCoreMapper toolSetCoreMapper;
    private final ConfigImporter configImporter;
    private final EntitySyncStateResolver entitySyncStateResolver;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreToolSet> getCoreToolSetWithHash(String toolSetName) {
        var toolSetWithHash = toolSetService.getToolSetWithHash(toolSetName);
        var coreToolSet = toolSetCoreMapper.mapToolSet(toolSetWithHash.model());
        return new CoreWithDomainHash<>(coreToolSet, toolSetWithHash.hash());
    }

    @Transactional
    public String updateToolSet(String toolSetName, CoreToolSet coreToolSet, String hash) {
        assertHashNotNull(toolSetName, hash);

        var toolSetWithHash = toolSetService.getToolSetWithHash(toolSetName);

        assertToolSetWasNotUpdated(toolSetWithHash, hash, OptimisticLockConflictException::onUpdate);
        importCoreToolSet(toolSetName, coreToolSet);

        return toolSetService.getToolSetWithHash(toolSetName).hash();
    }

    private void importCoreToolSet(String toolSetName, CoreToolSet coreToolSet) {
        Map<String, CoreToolSet> coreToolSets = new HashMap<>(1);
        coreToolSets.put(toolSetName, coreToolSet);

        Config config = new Config();
        config.setToolsets(coreToolSets);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String toolSetName, String hash) {
        assertHashNotNull(toolSetName, hash);

        var toolSetWithHash = toolSetService.getToolSetWithHash(toolSetName);
        assertToolSetWasNotUpdated(toolSetWithHash, hash, OptimisticLockConflictException::onGetSyncState);

        var toolSet = toolSetWithHash.model();
        var coreToolSet = toolSetCoreMapper.mapToolSet(toolSet);

        return entitySyncStateResolver.resolveForEntityInObject(
                coreToolSet,
                toolSet.getUpdatedAt(),
                "toolsets",
                toolSetName
        );
    }

    private void assertHashNotNull(String toolSetName, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    String.format("Hash must not be null. Use \"*\" to skip optimistic check. ToolSet:%s.", toolSetName)
            );
        }
    }

    private void assertToolSetWasNotUpdated(DomainObjectWithHash<ToolSet> toolSetWithHash,
                                            String expectedHash,
                                            Function<OptimisticLockConflictExceptionDetails, OptimisticLockConflictException> exceptionProvider) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        String currentHash = toolSetWithHash.hash();
        if (!expectedHash.equals(currentHash)) {
            String toolSetName = toolSetWithHash.model().getDeployment().getName();
            throw exceptionProvider.apply(new OptimisticLockConflictExceptionDetails("ToolSet", toolSetName, expectedHash, currentHash));
        }
    }
}
