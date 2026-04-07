package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException.OptimisticLockConflictExceptionDetails;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateResolver;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import com.epam.aidial.core.config.validation.SchemaConformToMetaSchemaValidator;
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
public class CoreApplicationTypeSchemaService {

    private final ApplicationTypeSchemaService schemaService;
    private final ApplicationTypeSchemaCoreMapper schemaCoreMapper;
    private final ConfigImporter configImporter;
    private final EntitySyncStateResolver entitySyncStateResolver;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreApplicationTypeSchema> getCoreSchemaWithHash(String id) {
        var schemaWithHash = schemaService.getSchemaWithHash(id);
        var coreApplicationTypeSchema = schemaCoreMapper.mapToCoreApplicationTypeSchema(schemaWithHash.model());
        return new CoreWithDomainHash<>(coreApplicationTypeSchema, schemaWithHash.hash());
    }

    @Transactional
    public String updateSchema(String id, CoreApplicationTypeSchema coreApplicationTypeSchema, String hash) {
        assertHashNotNull(id, hash);

        var schemaWithHash = schemaService.getSchemaWithHash(id);

        assertApplicationTypeSchemaWasNotUpdated(schemaWithHash, hash, OptimisticLockConflictException::onUpdate);
        importCoreApplicationTypeSchema(id, coreApplicationTypeSchema);

        return schemaService.getSchemaWithHash(id).hash();
    }

    private void importCoreApplicationTypeSchema(String id, CoreApplicationTypeSchema coreApplicationTypeSchema) {
        String applicationTypeSchemaAsString = schemaCoreMapper.toApplicationTypeSchemaAsString(coreApplicationTypeSchema);

        Map<String, String> coreApplicationTypeSchemas = new HashMap<>(1);
        coreApplicationTypeSchemas.put(id, applicationTypeSchemaAsString);

        Config config = new Config();
        config.setApplicationTypeSchemas(coreApplicationTypeSchemas);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String id, String hash) {
        assertHashNotNull(id, hash);

        var schemaWithHash = schemaService.getSchemaWithHash(id);
        assertApplicationTypeSchemaWasNotUpdated(schemaWithHash, hash, OptimisticLockConflictException::onGetSyncState);

        var schema = schemaWithHash.model();
        var coreApplicationTypeSchema = schemaCoreMapper.mapToCoreString(schema);
        boolean isSchemaValid = SchemaConformToMetaSchemaValidator.isValid(coreApplicationTypeSchema);

        return entitySyncStateResolver.resolveForEntityInArray(
                coreApplicationTypeSchema,
                isSchemaValid,
                schema.getUpdatedAt(),
                "applicationTypeSchemas",
                "$id",
                id
        );
    }

    private void assertHashNotNull(String id, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    String.format("Hash must not be null. Use \"*\" to skip optimistic check. ApplicationTypeSchema:%s.", id)
            );
        }
    }

    private void assertApplicationTypeSchemaWasNotUpdated(DomainObjectWithHash<ApplicationTypeSchema> schemaWithHash,
                                                          String expectedHash,
                                                          Function<OptimisticLockConflictExceptionDetails, OptimisticLockConflictException> exceptionProvider) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        String currentHash = schemaWithHash.hash();
        if (!expectedHash.equals(currentHash)) {
            String id = schemaWithHash.model().getSchemaId();
            throw exceptionProvider.apply(new OptimisticLockConflictExceptionDetails("ApplicationTypeSchema", id, expectedHash, currentHash));
        }
    }
}
