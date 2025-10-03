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
public class InterceptorRunnerImporter {

    private final InterceptorRunnerService interceptorRunnerService;

    public Collection<ImportComponent<InterceptorRunner>> importAdminInterceptorRunners(Map<String, InterceptorRunner> interceptorRunners,
                                                                                        ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(interceptorRunners)) {
            return interceptorRunners.entrySet().stream()
                    .map(interceptorRunnerEntry -> {
                                var interceptorRunner = interceptorRunnerEntry.getValue();
                                interceptorRunner.setName(interceptorRunnerEntry.getKey());
                                interceptorRunner.setDisplayName(interceptorRunnerEntry.getKey());
                                return processInterceptorRunner(interceptorRunnerEntry.getKey(), interceptorRunner, resolutionPolicy);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<InterceptorRunner> processInterceptorRunner(String interceptorRunnerName,
                                                                        InterceptorRunner newInterceptorRunner,
                                                                        ConflictResolutionPolicy resolutionPolicy) {
        Optional<InterceptorRunner> interceptorRunner = interceptorRunnerService.tryGet(interceptorRunnerName);
        if (interceptorRunner.isPresent()) {
            ImportAction importAction = handleExisting(newInterceptorRunner, resolutionPolicy, interceptorRunnerName);
            return new ImportComponent<>(importAction, interceptorRunner.get(), newInterceptorRunner);
        } else {
            interceptorRunnerService.create(newInterceptorRunner);
            return new ImportComponent<>(CREATE, null, newInterceptorRunner);
        }
    }

    private ImportAction handleExisting(InterceptorRunner newInterceptorRunner,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String interceptorRunnerName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing interceptor runner will remain unchanged.
            case OVERRIDE -> {
                interceptorRunnerService.update(interceptorRunnerName, newInterceptorRunner);
                yield UPDATE;
            }
        };
    }

    public List<ImportComponent<InterceptorRunner>> getActualImportedInterceptorRunners(Collection<ImportComponent<InterceptorRunner>> importComponents) {
        List<String> names = importComponents.stream()
                .map(ImportComponent::getNext)
                .map(InterceptorRunner::getName)
                .toList();
        Map<String, InterceptorRunner> importedInterceptorRunnersByNames = interceptorRunnerService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(InterceptorRunner::getName, Function.identity()));

        return importComponents.stream()
                .map(importComponent -> {
                    var next = importedInterceptorRunnersByNames.get(importComponent.getNext().getName());
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(InterceptorRunner interceptorRunner) {
        if (interceptorRunner != null) {
            interceptorRunner.setCreatedAt(null);
            interceptorRunner.setUpdatedAt(null);
        }
    }
}