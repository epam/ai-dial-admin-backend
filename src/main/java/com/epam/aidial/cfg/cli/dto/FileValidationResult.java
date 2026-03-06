package com.epam.aidial.cfg.cli.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FileValidationResult {
    String path;
    ValidationStatus status;
    String error;
    List<String> warnings;
}
