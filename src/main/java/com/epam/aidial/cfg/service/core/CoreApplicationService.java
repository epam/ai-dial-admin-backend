package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
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
public class CoreApplicationService {

    private final ApplicationService applicationService;
    private final ApplicationCoreMapper applicationCoreMapper;
    private final ConfigImporter configImporter;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreApplication> getCoreApplicationWithHash(String applicationName) {
        var applicationWithHash = applicationService.getApplicationWithHash(applicationName);
        var coreApplication = applicationCoreMapper.mapApplication(applicationWithHash.model());
        return new CoreWithDomainHash<>(coreApplication, applicationWithHash.hash());
    }

    @Transactional
    public String updateApplication(String applicationName, CoreApplication coreApplication, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Application:%s.", applicationName));
        }

        var applicationWithHash = applicationService.getApplicationWithHash(applicationName);

        assertNotConcurrencyOverwrite(applicationWithHash, hash);
        importCoreApplication(applicationName, coreApplication);

        return applicationService.getApplicationWithHash(applicationName).hash();
    }

    private void assertNotConcurrencyOverwrite(DomainObjectWithHash<Application> applicationWithHash,
                                               String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        Application application = applicationWithHash.model();
        String currentHash = applicationWithHash.hash();

        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: applicationName={}, expectedHash={}, currentHash={}",
                    application.getDeployment().getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Optimistic lock conflict on update: applicationName:'"
                    + "%s'. Please reload the data.", application.getDeployment().getName()));
        }
    }

    private void importCoreApplication(String applicationName, CoreApplication coreApplication) {
        Map<String, CoreApplication> coreApplications = new HashMap<>(1);
        coreApplications.put(applicationName, coreApplication);

        Config config = new Config();
        config.setApplications(coreApplications);

        configImporter.importConfigWithOverride(config);
    }
}
