package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.CatalogSchemaCoreMapper;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.service.CatalogSchemaService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException.OptimisticLockConflictExceptionDetails;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateResolver;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreCatalogSchema;
import com.epam.aidial.core.config.validation.CatalogSchemaConformToMetaSchemaValidator;
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
public class CoreCatalogSchemaService {

    private final CatalogSchemaService catalogSchemaService;
    private final CatalogSchemaCoreMapper schemaCoreMapper;
    private final ConfigImporter configImporter;
    private final EntitySyncStateResolver entitySyncStateResolver;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreCatalogSchema> getCoreSchemaWithHash(String id) {
        var schemaWithHash = catalogSchemaService.getSchemaWithHash(id);
        var coreCatalogSchema = schemaCoreMapper.mapToCoreCatalogSchema(schemaWithHash.model());
        return new CoreWithDomainHash<>(coreCatalogSchema, schemaWithHash.hash());
    }

    @Transactional
    public String updateSchema(String id, CoreCatalogSchema coreCatalogSchema, String hash) {
        assertHashNotNull(id, hash);

        var schemaWithHash = catalogSchemaService.getSchemaWithHash(id);

        assertCatalogSchemaWasNotUpdated(schemaWithHash, hash, OptimisticLockConflictException::onUpdate);
        importCoreCatalogSchema(id, coreCatalogSchema);

        return catalogSchemaService.getSchemaWithHash(id).hash();
    }

    private void importCoreCatalogSchema(String id, CoreCatalogSchema coreCatalogSchema) {
        String catalogSchemaAsString = schemaCoreMapper.toCatalogSchemaAsString(coreCatalogSchema);

        Map<String, String> coreCatalogSchemas = new HashMap<>(1);
        coreCatalogSchemas.put(id, catalogSchemaAsString);

        Config config = new Config();
        config.setCatalogSchemas(coreCatalogSchemas);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String id, String hash) {
        assertHashNotNull(id, hash);

        var schemaWithHash = catalogSchemaService.getSchemaWithHash(id);
        assertCatalogSchemaWasNotUpdated(schemaWithHash, hash, OptimisticLockConflictException::onGetSyncState);

        var schema = schemaWithHash.model();
        var coreCatalogSchema = schemaCoreMapper.mapToCoreString(schema);
        boolean isSchemaValid = CatalogSchemaConformToMetaSchemaValidator.isValid(coreCatalogSchema);

        return entitySyncStateResolver.resolveForEntityInArray(
                coreCatalogSchema,
                isSchemaValid,
                schema.getUpdatedAt(),
                "catalogSchemas",
                "$id",
                id
        );
    }

    private void assertHashNotNull(String id, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    String.format("Hash must not be null. Use \"*\" to skip optimistic check. CatalogSchema:%s.", id)
            );
        }
    }

    private void assertCatalogSchemaWasNotUpdated(DomainObjectWithHash<CatalogSchema> schemaWithHash,
                                                  String expectedHash,
                                                  Function<OptimisticLockConflictExceptionDetails, OptimisticLockConflictException> exceptionProvider) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        String currentHash = schemaWithHash.hash();
        if (!expectedHash.equals(currentHash)) {
            String id = schemaWithHash.model().getSchemaId();
            throw exceptionProvider.apply(new OptimisticLockConflictExceptionDetails("CatalogSchema", id, expectedHash, currentHash));
        }
    }
}
