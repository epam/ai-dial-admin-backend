package com.epam.aidial.cfg.cli;

import com.epam.aidial.cfg.cli.dto.FileValidationResult;
import com.epam.aidial.cfg.cli.dto.ValidateResult;
import com.epam.aidial.cfg.cli.dto.ValidationStatus;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.JsonConfigMerger;
import com.epam.aidial.cfg.service.config.transfer.MultiFileImportStrategy;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Component
@Command(
        name = "validate",
        mixinStandardHelpOptions = true,
        description = "Validate one or more DIAL Core JSON config files"
)
@Slf4j
@RequiredArgsConstructor
public class ValidateCommand implements Callable<Integer> {

    @Parameters(arity = "1..*", description = "Paths to DIAL Core JSON config files to validate")
    List<String> filePaths;

    @Option(names = "--strategy", defaultValue = "MERGE_JSON",
            description = "Multi-file strategy: MERGE_JSON (default) or SEQUENTIAL")
    MultiFileImportStrategy strategy = MultiFileImportStrategy.MERGE_JSON;

    @Option(names = "--conflict-resolution", defaultValue = "OVERRIDE",
            description = "Conflict resolution for SEQUENTIAL strategy: OVERRIDE (default) or SKIP")
    ConflictResolutionPolicy conflictResolution = ConflictResolutionPolicy.OVERRIDE;

    private final ConfigImporter configImporter;
    private final JsonConfigMerger jsonConfigMerger;
    private final ObjectMapper objectMapper;

    @Override
    public Integer call() {
        List<FileValidationResult> results = new ArrayList<>();
        boolean anyInvalid = false;

        try {
            if (strategy == MultiFileImportStrategy.MERGE_JSON) {
                FileValidationResult result = validateMerged(filePaths);
                results.add(result);
                if (result.getStatus() == ValidationStatus.INVALID) {
                    anyInvalid = true;
                }
            } else {
                for (String path : filePaths) {
                    FileValidationResult result = validateSingle(path);
                    results.add(result);
                    if (result.getStatus() == ValidationStatus.INVALID) {
                        anyInvalid = true;
                        break; // stop on first failure in sequential mode
                    }
                }
            }

            ValidateResult output = ValidateResult.builder()
                    .status(anyInvalid ? ValidationStatus.INVALID : ValidationStatus.VALID)
                    .strategy(strategy.name())
                    .files(results)
                    .build();
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(output));
            return anyInvalid ? 1 : 0;

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            log.error("Unexpected CLI error", e);
            return 2;
        }
    }

    private FileValidationResult validateMerged(List<String> paths) {
        String displayPath = paths.size() == 1 ? paths.get(0) : paths.toString();
        try {
            var config = jsonConfigMerger.merge(paths);
            configImporter.importConfigWithOverride(config);
            return FileValidationResult.builder().path(displayPath).status(ValidationStatus.VALID).build();
        } catch (IllegalArgumentException e) {
            return FileValidationResult.builder().path(displayPath).status(ValidationStatus.INVALID)
                    .error(e.getMessage()).build();
        } catch (Exception e) {
            return FileValidationResult.builder().path(displayPath).status(ValidationStatus.INVALID)
                    .error(toErrorMessage(e)).build();
        }
    }

    private FileValidationResult validateSingle(String path) {
        try {
            var config = jsonConfigMerger.merge(List.of(path));
            configImporter.importConfig(config, new ConfigImportOptions(conflictResolution));
            return FileValidationResult.builder().path(path).status(ValidationStatus.VALID).build();
        } catch (IllegalArgumentException e) {
            return FileValidationResult.builder().path(path).status(ValidationStatus.INVALID)
                    .error(e.getMessage()).build();
        } catch (Exception e) {
            return FileValidationResult.builder().path(path).status(ValidationStatus.INVALID)
                    .error(toErrorMessage(e)).build();
        }
    }

    private String toErrorMessage(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof JsonParseException jpe) {
            return "Invalid JSON at line " + jpe.getLocation().getLineNr() + ": " + jpe.getOriginalMessage();
        }
        if (cause instanceof JsonMappingException jme) {
            return "Invalid field '" + jme.getPathReference() + "': " + jme.getOriginalMessage();
        }
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
