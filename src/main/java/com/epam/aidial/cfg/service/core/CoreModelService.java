package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException.OptimisticLockConflictExceptionDetails;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateResolver;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreModel;
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
public class CoreModelService {

    private final ModelService modelService;
    private final ModelCoreMapper modelCoreMapper;
    private final ConfigImporter configImporter;
    private final EntitySyncStateResolver entitySyncStateResolver;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreModel> getCoreModelWithHash(String modelName) {
        var modelWithHash = modelService.getModelWithHash(modelName);
        var coreModel = modelCoreMapper.mapModel(modelWithHash.model());
        return new CoreWithDomainHash<>(coreModel, modelWithHash.hash());
    }

    @Transactional
    public String updateModel(String modelName, CoreModel coreModel, String hash) {
        assertHashNotNull(modelName, hash);

        var modelWithHash = modelService.getModelWithHash(modelName);

        assertModelWasNotUpdated(modelWithHash, hash, OptimisticLockConflictException::onUpdate);
        importCoreModel(modelName, coreModel);

        return modelService.getModelWithHash(modelName).hash();
    }

    private void importCoreModel(String modelName, CoreModel coreModel) {
        Map<String, CoreModel> coreModels = new HashMap<>(1);
        coreModels.put(modelName, coreModel);

        Config config = new Config();
        config.setModels(coreModels);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String modelName, String hash) {
        assertHashNotNull(modelName, hash);

        var modelWithHash = modelService.getModelWithHash(modelName);
        assertModelWasNotUpdated(modelWithHash, hash, OptimisticLockConflictException::onGetSyncState);

        var model = modelWithHash.model();
        var coreModel = modelCoreMapper.mapModel(model);

        return entitySyncStateResolver.resolve(
                coreModel,
                model.getUpdatedAt(),
                "models",
                modelName
        );
    }

    private void assertHashNotNull(String modelName, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    String.format("Hash must not be null. Use \"*\" to skip optimistic check. Model:%s.", modelName)
            );
        }
    }

    private void assertModelWasNotUpdated(DomainObjectWithHash<Model> modelWithHash,
                                          String expectedHash,
                                          Function<OptimisticLockConflictExceptionDetails, OptimisticLockConflictException> exceptionProvider) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        String currentHash = modelWithHash.hash();
        if (!expectedHash.equals(currentHash)) {
            String modelName = modelWithHash.model().getDeployment().getName();
            throw exceptionProvider.apply(new OptimisticLockConflictExceptionDetails("Model", modelName, expectedHash, currentHash));
        }
    }
}
