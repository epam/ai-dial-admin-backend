package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
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
public class CoreApplicationTypeSchemaService {

    private final ApplicationTypeSchemaService schemaService;
    private final ApplicationTypeSchemaCoreMapper schemaCoreMapper;
    private final ConfigImporter configImporter;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreApplicationTypeSchema> getCoreSchemaWithHash(String id) {
        var schemaWithHash = schemaService.getSchemaWithHash(id);
        var coreApplicationTypeSchema = schemaCoreMapper.mapToCoreApplicationTypeSchema(schemaWithHash.model());
        return new CoreWithDomainHash<>(coreApplicationTypeSchema, schemaWithHash.hash());
    }

    @Transactional
    public String updateSchema(String id, CoreApplicationTypeSchema coreApplicationTypeSchema, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Schema:%s.", id));
        }

        var schemaWithHash = schemaService.getSchemaWithHash(id);

        assertNotConcurrencyOverwrite(schemaWithHash, hash);
        importCoreApplicationTypeSchema(id, coreApplicationTypeSchema);

        return schemaService.getSchemaWithHash(id).hash();
    }

    private void assertNotConcurrencyOverwrite(DomainObjectWithHash<ApplicationTypeSchema> schemaWithHash,
                                               String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        ApplicationTypeSchema schema = schemaWithHash.model();
        String currentHash = schemaWithHash.hash();

        if (!expectedHash.equals(currentHash)) {
            throw OptimisticLockConflictException.onUpdate("ApplicationTypeSchema", schema.getSchemaId(), expectedHash, currentHash);
        }
    }

    private void importCoreApplicationTypeSchema(String id, CoreApplicationTypeSchema coreApplicationTypeSchema) {
        String applicationTypeSchemaAsString = schemaCoreMapper.toApplicationTypeSchemaAsString(coreApplicationTypeSchema);

        Map<String, String> coreApplicationTypeSchemas = new HashMap<>(1);
        coreApplicationTypeSchemas.put(id, applicationTypeSchemaAsString);

        Config config = new Config();
        config.setApplicationTypeSchemas(coreApplicationTypeSchemas);

        configImporter.importConfigWithOverride(config);
    }
}
