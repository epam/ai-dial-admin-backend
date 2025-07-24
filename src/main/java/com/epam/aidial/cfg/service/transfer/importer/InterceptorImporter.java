package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.InterceptorCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.service.ExternalDeploymentService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class InterceptorImporter {

    private final InterceptorService interceptorService;
    private final InterceptorCoreMapper interceptorCoreMapper;
    private final ExternalDeploymentService externalDeploymentService;

    public Collection<ImportComponent<Interceptor>> importInterceptors(Map<String, CoreInterceptor> coreInterceptors,
                                                                       ConflictResolutionPolicy resolutionPolicy,
                                                                       boolean isPreview) {
        if (MapUtils.isNotEmpty(coreInterceptors)) {
            Map<String, Interceptor> interceptors = coreInterceptors.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getValue())));
            return importAdminInterceptors(interceptors, resolutionPolicy, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Interceptor>> importAdminInterceptors(Map<String, Interceptor> interceptors,
                                                                            ConflictResolutionPolicy resolutionPolicy,
                                                                            boolean isPreview) {
        if (MapUtils.isNotEmpty(interceptors)) {
            return interceptors.entrySet().stream()
                    .map(interceptorEntry -> {
                                var interceptor = interceptorEntry.getValue();
                                interceptor.setName(interceptorEntry.getKey());
                                var importAction = processInterceptor(interceptorEntry.getKey(), interceptor, resolutionPolicy, isPreview);
                                return new ImportComponent<>(importAction, interceptor);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processInterceptor(String interceptorName,
                                            Interceptor newInterceptor,
                                            ConflictResolutionPolicy resolutionPolicy,
                                            boolean isPreview) {
        removeContainerSourceDependencyIfContainerIsAbsent(newInterceptor);
        if (interceptorService.exists(interceptorName)) {
            return handleExisting(newInterceptor, resolutionPolicy, interceptorName, isPreview);
        } else {
            return create(newInterceptor, isPreview);
        }
    }

    private void removeContainerSourceDependencyIfContainerIsAbsent(Interceptor newInterceptor) {
        if (!(newInterceptor.getSource() instanceof InterceptorContainerSource containerSource)) {
            return;
        }

        var deploymentInfo = externalDeploymentService.getByIdUncached(containerSource.getContainerId());
        if (deploymentInfo == null) {
            newInterceptor.setSource(null);
        }
    }

    private ImportAction handleExisting(Interceptor newInterceptor,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String interceptorName,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing interceptor will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    interceptorService.update(interceptorName, newInterceptor);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }


    private ImportAction create(Interceptor newInterceptor, boolean isPreview) {
        if (!isPreview) {
            interceptorService.create(newInterceptor);
        }
        return CREATE;
    }

    private Interceptor map(CoreInterceptor interceptor) {
        return interceptorCoreMapper.mapInterceptor(interceptor);
    }
}
