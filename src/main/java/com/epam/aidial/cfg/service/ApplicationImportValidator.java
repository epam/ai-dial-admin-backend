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
import java.util.function.Function;
import java.util.stream.Collectors;

@Validated
@Component
@LogExecution
public class ApplicationImportValidator {

    private record ApplicationNameAndVersion(String name, String version) {
    }

    private record ApplicationNameAndVersionAndPath(String name, String version, String folder) {
    }

    public void validateApplicationImport(ImportResources importApplications, @Valid ApplicationsEximDto applicationsEximDto) {
        validateApplicationUniqueness(applicationsEximDto.getApplications(), importApplications.isFlatImport());
    }

    private void validateApplicationUniqueness(List<ApplicationEximDto> applications, boolean isFlatImport) {
        if (isFlatImport) {
            validateUniquenessByNameAndVersion(applications);
        } else {
            validateUniquenessByNameAndVersionAndPath(applications);
        }
    }

    private void validateUniquenessByNameAndVersion(List<ApplicationEximDto> applications) {
        var duplicatedApplicationNames = getDuplicateApplicationNames(applications);

        if (duplicatedApplicationNames.isEmpty()) {
            return;
        }

        var errorMessage = new StringBuilder("Application uniqueness violation. Conflicts found:");
        duplicatedApplicationNames.forEach(application ->
                errorMessage.append("\n - Duplicated application name '%s' and version '%s'"
                        .formatted(application.name, application.version)));
        throw new IllegalArgumentException(errorMessage.toString());
    }

    private void validateUniquenessByNameAndVersionAndPath(List<ApplicationEximDto> applications) {
        var duplicatedApplicationNames = getDuplicateApplicationNamesAndPath(applications);

        if (duplicatedApplicationNames.isEmpty()) {
            return;
        }

        var errorMessage = new StringBuilder("Application uniqueness violation. Conflicts found:");
        duplicatedApplicationNames.forEach(application ->
                errorMessage.append("\n - Duplicated application name '%s' and version '%s' and folder '%s'"
                        .formatted(application.name, application.version, application.folder)));
        throw new IllegalArgumentException(errorMessage.toString());
    }

    private List<ApplicationNameAndVersion> getDuplicateApplicationNames(List<ApplicationEximDto> applications) {
        var counts = applications.stream()
                .map(application -> new ApplicationNameAndVersion(application.getName(), application.getVersion()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return counts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey).toList();
    }

    private List<ApplicationNameAndVersionAndPath> getDuplicateApplicationNamesAndPath(List<ApplicationEximDto> applications) {
        var counts = applications.stream()
                .map(app -> new ApplicationNameAndVersionAndPath(app.getName(), app.getVersion(), app.getFolderId()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return counts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey).toList();
    }

}