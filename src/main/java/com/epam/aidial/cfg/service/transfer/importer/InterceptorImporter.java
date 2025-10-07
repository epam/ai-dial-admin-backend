package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.InterceptorCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    private final DeploymentManagerService deploymentManagerService;

    public Collection<ImportComponent<Interceptor>> importInterceptors(Map<String, CoreInterceptor> coreInterceptors,
                                                                       ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(coreInterceptors)) {
            Map<String, Interceptor> interceptors = coreInterceptors.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getValue())));
            return importAdminInterceptors(interceptors, resolutionPolicy);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Interceptor>> importAdminInterceptors(Map<String, Interceptor> interceptors,
                                                                            ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(interceptors)) {
            return interceptors.entrySet().stream()
                    .map(interceptorEntry -> {
                                var interceptor = interceptorEntry.getValue();
                                interceptor.setName(interceptorEntry.getKey());
                                return processInterceptor(interceptorEntry.getKey(), interceptor, resolutionPolicy);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<Interceptor> processInterceptor(String interceptorName,
                                                            Interceptor newInterceptor,
                                                            ConflictResolutionPolicy resolutionPolicy) {
        removeContainerSourceDependencyIfContainerIsAbsent(newInterceptor);

        Optional<Interceptor> interceptor = interceptorService.tryGet(interceptorName);
        if (interceptor.isPresent()) {
            ImportAction importAction = handleExisting(newInterceptor, resolutionPolicy, interceptorName);
            return new ImportComponent<>(importAction, interceptor.get(), newInterceptor);
        } else {
            interceptorService.create(newInterceptor);
            return new ImportComponent<>(CREATE, null, newInterceptor);
        }
    }

    private void removeContainerSourceDependencyIfContainerIsAbsent(Interceptor newInterceptor) {
        if (!(newInterceptor.getSource() instanceof InterceptorContainerSource containerSource)) {
            return;
        }

        String containerId = containerSource.getContainerId();

        DeploymentInfoDto deploymentInfo = null;
        try {
            deploymentInfo = deploymentManagerService.getById(containerId);
        } catch (DeploymentClientNotExistsException e) {
            log.warn("Failed to get deployment by ID '%s' on Interceptor '%s' import"
                    .formatted(containerId, newInterceptor.getName()), e);
        }

        if (deploymentInfo == null) {
            newInterceptor.setSource(null);
        }
    }

    private ImportAction handleExisting(Interceptor newInterceptor,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String interceptorName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing interceptor will remain unchanged.
            case OVERRIDE -> {
                interceptorService.update(interceptorName, newInterceptor);
                yield UPDATE;
            }
        };
    }

    private Interceptor map(CoreInterceptor interceptor) {
        return interceptorCoreMapper.mapInterceptor(interceptor);
    }

    public List<ImportComponent<Interceptor>> getActualImportedInterceptors(Collection<ImportComponent<Interceptor>> importComponents) {
        List<String> names = importComponents.stream()
                .map(ImportComponent::getNext)
                .map(Interceptor::getName)
                .toList();
        Map<String, Interceptor> importedInterceptorsByNames = interceptorService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(Interceptor::getName, Function.identity()));

        return importComponents.stream()
                .map(importComponent -> {
                    var next = importedInterceptorsByNames.get(importComponent.getNext().getName());
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Interceptor interceptor) {
        if (interceptor != null) {
            interceptor.setCreatedAt(null);
            interceptor.setUpdatedAt(null);
        }
    }
}
