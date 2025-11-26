package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.InterceptorCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreInterceptor;
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
public class CoreInterceptorService {

    private final InterceptorService interceptorService;
    private final InterceptorCoreMapper interceptorCoreMapper;
    private final ConfigImporter configImporter;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreInterceptor> getCoreInterceptorWithHash(String interceptorName) {
        var interceptorWithHash = interceptorService.getInterceptorWithHash(interceptorName);
        var coreInterceptor = interceptorCoreMapper.mapInterceptor(interceptorWithHash.model());
        return new CoreWithDomainHash<>(coreInterceptor, interceptorWithHash.hash());
    }

    @Transactional
    public String updateInterceptor(String interceptorName, CoreInterceptor coreInterceptor, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Interceptor:%s.", interceptorName));
        }

        var interceptorWithHash = interceptorService.getInterceptorWithHash(interceptorName);

        assertNotConcurrencyOverwrite(interceptorWithHash, hash);
        importCoreInterceptor(interceptorName, coreInterceptor);

        return interceptorService.getInterceptorWithHash(interceptorName).hash();
    }

    private void assertNotConcurrencyOverwrite(DomainObjectWithHash<Interceptor> interceptorWithHash,
                                               String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        Interceptor interceptor = interceptorWithHash.model();
        String currentHash = interceptorWithHash.hash();

        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: interceptorName={}, expectedHash={}, currentHash={}",
                    interceptor.getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Optimistic lock conflict on update: interceptorName:'"
                    + "%s'. Please reload the data.", interceptor.getName()));
        }
    }

    private void importCoreInterceptor(String interceptorName, CoreInterceptor coreInterceptor) {
        Map<String, CoreInterceptor> coreInterceptors = new HashMap<>(1);
        coreInterceptors.put(interceptorName, coreInterceptor);

        Config config = new Config();
        config.setInterceptors(coreInterceptors);

        configImporter.importConfigWithOverride(config);
    }
}
