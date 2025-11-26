package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.utils.PathUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Validated
@Component
@LogExecution
public class ApplicationImportValidator {

    public void validateApplicationImport(ImportResources importApplications, @Valid ApplicationsEximDto applicationsEximDto) {
        validateApplicationUniqueness(applicationsEximDto.getApplications(), importApplications.isFlatImport());
    }

    private void validateApplicationUniqueness(List<ApplicationEximDto> applications, boolean isFlatImport) {
        var duplicatedApplicationIds = getDuplicatedApplicationIds(applications);
        var duplicatedApplicationNames = Map.<String, Set<String>>of();
        if (isFlatImport) {
            duplicatedApplicationNames = getDuplicateApplicationNames(applications);
        }

        if (duplicatedApplicationIds.isEmpty() && duplicatedApplicationNames.isEmpty()) {
            return;
        }

        var errorMessage = new StringBuilder("Application uniqueness violation. Conflicts found:");
        if (!duplicatedApplicationIds.isEmpty()) {
            errorMessage.append("\n  - Duplicated application IDs: %s".formatted(duplicatedApplicationIds));
        }
        if (!duplicatedApplicationNames.isEmpty()) {
            duplicatedApplicationNames.forEach((applicationName, applicationIds) ->
                    errorMessage.append("\n  - Duplicated application name %s for IDs: %s".formatted(applicationName, applicationIds))
            );
        }
        throw new IllegalArgumentException(errorMessage.toString());
    }

    private Set<String> getDuplicatedApplicationIds(List<ApplicationEximDto> applications) {
        var idCounts = applications.stream()
                .map(ApplicationEximDto::getApplicationTypeSchemaId)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return idCounts.entrySet().stream()
                .filter(countEntry -> countEntry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Map<String, Set<String>> getDuplicateApplicationNames(List<ApplicationEximDto> applications) {
        var nameCounts = applications.stream()
                .map(ApplicationEximDto::getName)
                .collect(Collectors.groupingBy(id -> PathUtils.parseVersionedPath(id).getVersionedName(), Collectors.toSet()));

        return nameCounts.entrySet().stream()
                .filter(countEntry -> countEntry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}