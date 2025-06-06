package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import com.epam.aidial.cfg.domain.service.InterceptorRunnerService;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class InterceptorRunnerImporter {

    private final InterceptorRunnerService interceptorRunnerService;

    public Collection<ImportComponent<InterceptorRunner>> importAdminInterceptorRunners(Map<String, InterceptorRunner> interceptorRunners,
                                                                                        ConflictResolutionPolicy resolutionPolicy,
                                                                                        boolean isPreview) {
        if (MapUtils.isNotEmpty(interceptorRunners)) {
            return interceptorRunners.entrySet().stream()
                    .map(interceptorRunnerEntry -> {
                                var interceptorRunner = interceptorRunnerEntry.getValue();
                                interceptorRunner.setName(interceptorRunnerEntry.getKey());
                                var importAction = processInterceptorRunner(interceptorRunnerEntry.getKey(), interceptorRunner, resolutionPolicy, isPreview);
                                return new ImportComponent<>(importAction, interceptorRunner);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processInterceptorRunner(String interceptorRunnerName,
                                                  InterceptorRunner newInterceptorRunner,
                                                  ConflictResolutionPolicy resolutionPolicy,
                                                  boolean isPreview) {
        if (interceptorRunnerService.exists(interceptorRunnerName)) {
            return handleExisting(newInterceptorRunner, resolutionPolicy, interceptorRunnerName, isPreview);
        } else {
            return create(newInterceptorRunner, isPreview);
        }
    }

    private ImportAction handleExisting(InterceptorRunner newInterceptorRunner,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String interceptorRunnerName,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing interceptor runner will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    interceptorRunnerService.update(interceptorRunnerName, newInterceptorRunner);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private ImportAction create(InterceptorRunner newInterceptorRunner, boolean isPreview) {
        if (!isPreview) {
            interceptorRunnerService.create(newInterceptorRunner);
        }
        return CREATE;
    }
}