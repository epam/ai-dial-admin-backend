package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Collection<String> getAllDescriptionKeywords() {
        var modelKeywords = modelService.getAllModels().stream()
                .map(Model::getTopics)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        var applicationKeywords = applicationService.getAllApplications().stream()
                .map(Application::getDescriptionKeywords)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        var appRunnerKeywords = applicationTypeSchemaService.getAll().stream()
                .map(ApplicationTypeSchema::getTopics)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return combineAndSortSets(modelKeywords, applicationKeywords, appRunnerKeywords);
    }

    public static List<String> combineAndSortSets(Set<String> set1, Set<String> set2, Set<String> set3) {
        TreeSet<String> combinedSet = new TreeSet<>(set1);
        combinedSet.addAll(set2);
        combinedSet.addAll(set3);
        return List.copyOf(combinedSet);
    }

}
