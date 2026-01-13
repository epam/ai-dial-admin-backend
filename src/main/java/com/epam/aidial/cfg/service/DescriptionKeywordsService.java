package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.InterceptorRunnerService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@LogExecution
public class DescriptionKeywordsService {

    private final ModelService modelService;
    private final ApplicationService applicationService;
    private final ApplicationTypeSchemaService applicationTypeSchemaService;
    private final ToolSetService toolSetService;
    private final AdapterService adapterService;
    private final InterceptorService interceptorService;
    private final InterceptorRunnerService interceptorRunnerService;
    private final KeyService keyService;
    private final RoleService roleService;
    private final RouteService routeService;

    public Collection<String> getAllDescriptionKeywords() {
        var modelKeywords = modelService.getAll().stream()
                .map(Model::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var applicationKeywords = applicationService.getAllApplications().stream()
                .map(Application::getDescriptionKeywords)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var appRunnerKeywords = applicationTypeSchemaService.getAll().stream()
                .map(ApplicationTypeSchema::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var toolSetKeywords = toolSetService.getAll().stream()
                .map(ToolSet::getDescriptionKeywords)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var adaptersTopics = adapterService.getAll().stream()
                .map(Adapter::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var interceptorTopics = interceptorService.getAll().stream()
                .map(Interceptor::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var interceptorRunnerTopics = interceptorRunnerService.getAll().stream()
                .map(InterceptorRunner::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var keyTopics = keyService.getAllKeys().stream()
                .map(Key::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var roleTopics = roleService.getAllRoles().stream()
                .map(Role::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var routeTopics = routeService.getAll().stream()
                .map(Route::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return combineAndSortSets(modelKeywords, applicationKeywords, appRunnerKeywords, toolSetKeywords, adaptersTopics,
                interceptorTopics, interceptorRunnerTopics, keyTopics, roleTopics, routeTopics);
    }

    @SafeVarargs
    private List<String> combineAndSortSets(Set<String>... sets) {
        return Arrays.stream(sets)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(TreeSet::new),
                        List::copyOf
                ));
    }
}