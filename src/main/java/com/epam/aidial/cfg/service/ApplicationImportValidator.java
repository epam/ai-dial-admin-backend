package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.model.ImportResources;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Validated
@Component
@LogExecution
public class ApplicationImportValidator {

    public void validateApplicationImport(ImportResources importApplications, @Valid ApplicationsEximDto applicationsEximDto) {
        validateApplicationUniqueness(applicationsEximDto.getApplications(), importApplications.isFlatImport());
    }

    private void validateApplicationUniqueness(List<ApplicationEximDto> applications, boolean isFlatImport) {
        var duplicatedApplicationNames = Map.<String, String>of();

        duplicatedApplicationNames = isFlatImport
                ? getDuplicateApplicationNames(applications)
                : getDuplicateApplicationNamesAndPath(applications);

        if (duplicatedApplicationNames.isEmpty()) {
            return;
        }

        var errorMessage = new StringBuilder("Application uniqueness violation. Conflicts found:");

        if (!duplicatedApplicationNames.isEmpty()) {
            duplicatedApplicationNames.forEach((applicationName, applicationVersion) ->
                    errorMessage.append("Duplicated application name %s and version  %s".formatted(applicationName, applicationVersion))
            );
        }
        throw new IllegalArgumentException(errorMessage.toString());
    }

    private Map<String, String> getDuplicateApplicationNames(List<ApplicationEximDto> applications) {
        var counts = applications.stream()
                .map(application -> Map.entry(application.getName(), application.getVersion()))
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        return counts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .collect(Collectors.toMap(
                        e -> e.getKey().getKey(),
                        e -> e.getKey().getValue()
                ));
    }

    private Map<String, String> getDuplicateApplicationNamesAndPath(List<ApplicationEximDto> applications) {
        var counts = applications.stream()
                .map(app -> List.of(app.getName(), app.getVersion(), app.getFolderId()))
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        return counts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .collect(Collectors.toMap(
                        e -> e.getKey().get(0),
                        e -> e.getKey().get(1)
                ));
    }

}