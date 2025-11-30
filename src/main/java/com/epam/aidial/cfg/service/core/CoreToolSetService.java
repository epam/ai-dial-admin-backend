package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ToolSetCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreToolSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service
@RequiredArgsConstructor
@Slf4j
public class CoreToolSetService {

    private final ToolSetService toolSetService;
    private final ToolSetCoreMapper toolSetCoreMapper;
    private final ConfigImporter configImporter;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreToolSet> getCoreToolSetWithHash(String toolSetName) {
        var toolSetWithHash = toolSetService.getToolSetWithHash(toolSetName);
        var coreToolSet = toolSetCoreMapper.mapToolSet(toolSetWithHash.model());
        return new CoreWithDomainHash<>(coreToolSet, toolSetWithHash.hash());
    }

    @Transactional
    public String updateToolSet(String toolSetName, CoreToolSet coreToolSet, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    "Hash must not be null. Use \"*\" to skip optimistic check.");
        }

        var toolSetWithHash = toolSetService.getToolSetWithHash(toolSetName);

        assertNotConcurrencyOverwrite(toolSetWithHash, hash);
        importCoreToolSet(toolSetName, coreToolSet);

        return toolSetService.getToolSetWithHash(toolSetName).hash();
    }

    private void assertNotConcurrencyOverwrite(DomainObjectWithHash<ToolSet> toolSetWithHash, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        ToolSet toolSet = toolSetWithHash.model();
        String currentHash = toolSetWithHash.hash();

        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: toolSetName={}, expectedHash={}, currentHash={}",
                    toolSet.getDeployment().getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Unable to update ToolSet '%s'. The data may have been modified by another user, or the name/ID may already exist. Please reload the data and try again.",
                    toolSet.getDeployment().getName()));
        }
    }

    private void importCoreToolSet(String toolSetName, CoreToolSet coreToolSet) {
        Map<String, CoreToolSet> coreToolSets = new HashMap<>(1);
        coreToolSets.put(toolSetName, coreToolSet);

        Config config = new Config();
        config.setToolsets(coreToolSets);

        configImporter.importConfigWithOverride(config);
    }
}
