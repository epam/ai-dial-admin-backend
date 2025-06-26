package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.PromptEximDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
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
public class PromptImportValidator {

    public void validatePromptImport(ImportResources importPrompts, @Valid PromptsEximDto promptsEximDto) {
        validatePromptUniqueness(promptsEximDto.getPrompts(), importPrompts.isFlatImport());
    }

    private void validatePromptUniqueness(List<PromptEximDto> prompts, boolean isFlatImport) {
        var duplicatedPromptIds = getDuplicatedPromptIds(prompts);
        var duplicatedPromptNames = Map.<String, Set<String>>of();
        if (isFlatImport) {
            duplicatedPromptNames = getDuplicatedPromptNames(prompts);
        }

        if (duplicatedPromptIds.isEmpty() && duplicatedPromptNames.isEmpty()) {
            return;
        }

        var errorMessage = new StringBuilder("Prompt uniqueness violation. Conflicts found:");
        if (!duplicatedPromptIds.isEmpty()) {
            errorMessage.append("\n  - Duplicated prompt IDs: %s".formatted(duplicatedPromptIds));
        }
        if (!duplicatedPromptNames.isEmpty()) {
            duplicatedPromptNames.forEach((promptName, promptIds) ->
                    errorMessage.append("\n  - Duplicated prompt name %s for IDs: %s".formatted(promptName, promptIds))
            );
        }
        throw new IllegalArgumentException(errorMessage.toString());
    }

    private Set<String> getDuplicatedPromptIds(List<PromptEximDto> prompts) {
        var idCounts = prompts.stream()
                .map(PromptEximDto::getId)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return idCounts.entrySet().stream()
                .filter(countEntry -> countEntry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Map<String, Set<String>> getDuplicatedPromptNames(List<PromptEximDto> prompts) {
        var nameCounts = prompts.stream()
                .map(PromptEximDto::getId)
                .collect(Collectors.groupingBy(id -> PathUtils.parseVersionedPath(id).getVersionedName(), Collectors.toSet()));

        return nameCounts.entrySet().stream()
                .filter(countEntry -> countEntry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
