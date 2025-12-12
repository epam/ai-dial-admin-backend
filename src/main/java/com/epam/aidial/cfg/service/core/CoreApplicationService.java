package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException.OptimisticLockConflictExceptionDetails;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateResolver;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
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
public class CoreApplicationService {

    private final ApplicationService applicationService;
    private final ApplicationCoreMapper applicationCoreMapper;
    private final ConfigImporter configImporter;
    private final EntitySyncStateResolver entitySyncStateResolver;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreApplication> getCoreApplicationWithHash(String applicationName) {
        var applicationWithHash = applicationService.getApplicationWithHash(applicationName);
        var coreApplication = applicationCoreMapper.mapApplication(applicationWithHash.model());
        return new CoreWithDomainHash<>(coreApplication, applicationWithHash.hash());
    }

    @Transactional
    public String updateApplication(String applicationName, CoreApplication coreApplication, String hash) {
        assertHashNotNull(applicationName, hash);

        var applicationWithHash = applicationService.getApplicationWithHash(applicationName);

        assertApplicationWasNotUpdated(applicationWithHash, hash, OptimisticLockConflictException::onUpdate);
        importCoreApplication(applicationName, coreApplication);

        return applicationService.getApplicationWithHash(applicationName).hash();
    }

    private void importCoreApplication(String applicationName, CoreApplication coreApplication) {
        Map<String, CoreApplication> coreApplications = new HashMap<>(1);
        coreApplications.put(applicationName, coreApplication);

        Config config = new Config();
        config.setApplications(coreApplications);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String applicationName, String hash) {
        assertHashNotNull(applicationName, hash);

        var applicationWithHash = applicationService.getApplicationWithHash(applicationName);
        assertApplicationWasNotUpdated(applicationWithHash, hash, OptimisticLockConflictException::onGetSyncState);

        var application = applicationWithHash.model();
        var coreApplication = applicationCoreMapper.mapApplication(application);
        boolean isApplicationValid = application.getValidityState().isValid();

        return entitySyncStateResolver.resolve(
                coreApplication,
                isApplicationValid,
                application.getUpdatedAt(),
                "applications",
                applicationName
        );
    }

    private void assertHashNotNull(String applicationName, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    String.format("Hash must not be null. Use \"*\" to skip optimistic check. Application:%s.", applicationName)
            );
        }
    }

    private void assertApplicationWasNotUpdated(DomainObjectWithHash<Application> applicationWithHash,
                                                String expectedHash,
                                                Function<OptimisticLockConflictExceptionDetails, OptimisticLockConflictException> exceptionProvider) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        String currentHash = applicationWithHash.hash();
        if (!expectedHash.equals(currentHash)) {
            String applicationName = applicationWithHash.model().getDeployment().getName();
            throw exceptionProvider.apply(new OptimisticLockConflictExceptionDetails("Application", applicationName, expectedHash, currentHash));
        }
    }
}
