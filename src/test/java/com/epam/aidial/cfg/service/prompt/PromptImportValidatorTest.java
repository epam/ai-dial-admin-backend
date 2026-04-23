package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.dto.PromptEximDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PromptImportValidatorTest {

    @InjectMocks
    private PromptImportValidator promptImportValidator;

    @Test
    @SneakyThrows
    void validatePromptImport_FolderNameEndsWithDot_ThrowValidationError() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        // Create a PromptEximDto with path traversal
        var promptExim = new PromptEximDto();
        promptExim.setId("prompts/public/../../PROMPT 1__1.0.0");
        promptExim.setDescription("Test description");
        promptExim.setContent("Test content");
        var promptsExim = new PromptsEximDto();
        promptsExim.setPrompts(List.of(promptExim));
        promptsExim.setFolders(List.of());

        // when
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var executableValidator = validator.forExecutables();
        var method = promptImportValidator.getClass().getMethod("collectUniquenessConflicts", ImportResources.class, PromptsEximDto.class);
        var violations = executableValidator.validateParameters(promptImportValidator, method, new Object[]{importPrompts, promptsExim});

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations).first().extracting(ConstraintViolation::getMessage)
                .isEqualTo("Resource name and/or parent folders must not end with .(dot)");
    }

    @Test
    @SneakyThrows
    void collectUniquenessConflicts_PathNotStartsWithPromptsPublic_ThrowValidationError() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        // Create a PromptEximDto with invalid path
        var promptExim = new PromptEximDto();
        promptExim.setId("prompts/test/PROMPT 1__1.0.0");
        promptExim.setDescription("Test description");
        promptExim.setContent("Test content");
        var promptsExim = new PromptsEximDto();
        promptsExim.setPrompts(List.of(promptExim));
        promptsExim.setFolders(List.of());

        // when
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var executableValidator = validator.forExecutables();
        var method = promptImportValidator.getClass().getMethod("collectUniquenessConflicts", ImportResources.class, PromptsEximDto.class);
        var violations = executableValidator.validateParameters(promptImportValidator, method, new Object[]{importPrompts, promptsExim});

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations).first().extracting(ConstraintViolation::getMessage)
                .isEqualTo("must match \"prompts/public/([^/]+/)*[^/]+__[^/]+\"");
    }

    @Test
    @SneakyThrows
    void collectUniquenessConflicts_DuplicatedIds_ReturnsConflictPerId() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        // Create PromptEximDtos with the same ID
        var promptExim1 = new PromptEximDto();
        promptExim1.setId("prompts/public/PROMPT 1__1.0.0");
        promptExim1.setContent("Test content one");
        var promptExim2 = new PromptEximDto();
        promptExim2.setId("prompts/public/PROMPT 1__1.0.0");
        promptExim2.setContent("Test content two");
        var promptsExim = new PromptsEximDto();
        promptsExim.setPrompts(List.of(promptExim1, promptExim2));
        promptsExim.setFolders(List.of());

        // when/then
        Map<String, String> conflicts = promptImportValidator.collectUniquenessConflicts(importPrompts, promptsExim);

        assertThat(conflicts).containsOnlyKeys("prompts/public/PROMPT 1__1.0.0");
        assertThat(conflicts.get("prompts/public/PROMPT 1__1.0.0"))
                .isEqualTo("Duplicated prompt id: \"prompts/public/PROMPT 1__1.0.0\" appears 2 time(s) in the import file.");
    }

    @Test
    @SneakyThrows
    void collectUniquenessConflicts_FlatImportAndDuplicatedName_ReturnsConflictForEachSource() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .flatImport(true)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        // Create PromptEximDtos with the same name
        var promptExim1 = new PromptEximDto();
        promptExim1.setId("prompts/public/test1/PROMPT 1__1.0.0");
        promptExim1.setContent("Test content one");
        var promptExim2 = new PromptEximDto();
        promptExim2.setId("prompts/public/test2/PROMPT 1__1.0.0");
        promptExim2.setContent("Test content two");
        var promptsExim = new PromptsEximDto();
        promptsExim.setPrompts(List.of(promptExim1, promptExim2));
        promptsExim.setFolders(List.of());

        // when/then
        Map<String, String> conflicts = promptImportValidator.collectUniquenessConflicts(importPrompts, promptsExim);

        assertThat(conflicts).containsOnlyKeys(
                "prompts/public/test1/PROMPT 1__1.0.0",
                "prompts/public/test2/PROMPT 1__1.0.0"
        );
        assertThat(conflicts.get("prompts/public/test1/PROMPT 1__1.0.0"))
                .isEqualTo("Duplicated prompt name PROMPT 1__1.0.0 for IDs: prompts/public/test2/PROMPT 1__1.0.0, prompts/public/test1/PROMPT 1__1.0.0");
        assertThat(conflicts.get("prompts/public/test2/PROMPT 1__1.0.0"))
                .isEqualTo("Duplicated prompt name PROMPT 1__1.0.0 for IDs: prompts/public/test2/PROMPT 1__1.0.0, prompts/public/test1/PROMPT 1__1.0.0");
    }

    @Test
    @SneakyThrows
    void collectUniquenessConflicts_FlatImportAndDuplicatedIdAndName_MergesMessages() {
        // given
        var importPrompts = ImportResources.builder()
                .path("public/test/")
                .flatImport(true)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();

        // Create PromptEximDtos with the same ID and name
        var promptExim1 = new PromptEximDto();
        promptExim1.setId("prompts/public/test1/PROMPT 1__1.0.0");
        promptExim1.setContent("Test content one");
        var promptExim2 = new PromptEximDto();
        promptExim2.setId("prompts/public/test2/PROMPT 1__1.0.0");
        promptExim2.setContent("Test content two");
        var promptExim3 = new PromptEximDto();
        promptExim3.setId("prompts/public/test2/PROMPT 1__1.0.0");
        promptExim3.setContent("Test content three");
        var promptsExim = new PromptsEximDto();
        promptsExim.setPrompts(List.of(promptExim1, promptExim2, promptExim3));
        promptsExim.setFolders(List.of());

        // when/then
        Map<String, String> conflicts = promptImportValidator.collectUniquenessConflicts(importPrompts, promptsExim);

        assertThat(conflicts).containsOnlyKeys(
                "prompts/public/test1/PROMPT 1__1.0.0",
                "prompts/public/test2/PROMPT 1__1.0.0"
        );
        assertThat(conflicts.get("prompts/public/test1/PROMPT 1__1.0.0"))
                .contains("Duplicated prompt name PROMPT 1__1.0.0 for IDs: prompts/public/test2/PROMPT 1__1.0.0, prompts/public/test1/PROMPT 1__1.0.0");
        assertThat(conflicts.get("prompts/public/test2/PROMPT 1__1.0.0"))
                .isEqualTo("""
                        Duplicated prompt id: "prompts/public/test2/PROMPT 1__1.0.0" appears 2 time(s) in the import file.
                        Duplicated prompt name PROMPT 1__1.0.0 for IDs: prompts/public/test2/PROMPT 1__1.0.0, prompts/public/test1/PROMPT 1__1.0.0""");
    }

}