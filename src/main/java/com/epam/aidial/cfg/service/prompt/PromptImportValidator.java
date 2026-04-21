package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.PromptEximDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.utils.PathUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Validated
@Component
@LogExecution
public class PromptImportValidator {

    public Map<String, String> collectUniquenessConflicts(ImportResources importPrompts, @Valid PromptsEximDto promptsEximDto) {
        return collectUniquenessConflicts(promptsEximDto.getPrompts(), importPrompts.isFlatImport());
    }

    private Map<String, String> collectUniquenessConflicts(List<PromptEximDto> prompts, boolean isFlatImport) {
        var duplicatedPromptIds = getDuplicatedPromptIds(prompts);
        var duplicatedPromptNames = Map.<String, Set<String>>of();
        if (isFlatImport) {
            duplicatedPromptNames = getDuplicatedPromptNames(prompts);
        }

        if (duplicatedPromptIds.isEmpty() && duplicatedPromptNames.isEmpty()) {
            return Map.of();
        }

        var errorsByPromptId = new LinkedHashMap<String, String>();
        duplicatedPromptIds.forEach((id, count) ->
                addError(errorsByPromptId, id, "Duplicate prompt id: \"%s\" appears %d in the import file."
                        .formatted(id, count))
        );
        duplicatedPromptNames.forEach((promptName, promptIds) -> {
            var message = "Duplicated prompt name %s for IDs: %s"
                    .formatted(promptName, String.join(", ", promptIds));
            promptIds.forEach(id -> addError(errorsByPromptId, id, message));
        });
        return errorsByPromptId;
    }

    private void addError(Map<String, String> errorsByPromptId, String id, String message) {
        errorsByPromptId.merge(id, message,
                (oldMsg, newMsg) -> oldMsg + "\n" + newMsg
        );
    }

    private Map<String, Long> getDuplicatedPromptIds(List<PromptEximDto> prompts) {
        return prompts.stream()
                .map(PromptEximDto::getId)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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