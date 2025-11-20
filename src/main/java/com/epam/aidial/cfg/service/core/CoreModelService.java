package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreModel;
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
public class CoreModelService {

    private final ModelService modelService;
    private final ModelCoreMapper modelCoreMapper;
    private final ConfigImporter configImporter;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreModel> getCoreModelWithHash(String modelName) {
        var modelWithHash = modelService.getModelWithHash(modelName);
        var coreModel = modelCoreMapper.mapModel(modelWithHash.model());
        return new CoreWithDomainHash<>(coreModel, modelWithHash.hash());
    }

    @Transactional
    public String updateModel(String modelName, CoreModel coreModel, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    "Hash must not be null. Use \"*\" to skip optimistic check.");
        }

        var modelWithHash = modelService.getModelWithHash(modelName);

        assertNotConcurrencyOverwrite(modelWithHash, hash);
        importCoreModel(modelName, coreModel);

        return modelService.getModelWithHash(modelName).hash();
    }

    private void assertNotConcurrencyOverwrite(DomainObjectWithHash<Model> modelWithHash, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        Model model = modelWithHash.model();
        String currentHash = modelWithHash.hash();

        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: modelName={}, expectedHash={}, currentHash={}",
                    model.getDeployment().getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException("Optimistic lock conflict on update: modelName:'"
                    + model.getDeployment().getName() + "'. Please reload the data.");

        }
    }

    private void importCoreModel(String modelName, CoreModel coreModel) {
        Map<String, CoreModel> coreModels = new HashMap<>(1);
        coreModels.put(modelName, coreModel);

        Config config = new Config();
        config.setModels(coreModels);

        configImporter.importConfigWithOverride(config);
    }
}
