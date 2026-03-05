package com.epam.aidial.cfg.cli.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class ValidateResult {
    String status;
    String strategy;
    List<FileValidationResult> files;
}
