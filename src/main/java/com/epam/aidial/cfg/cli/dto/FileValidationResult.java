package com.epam.aidial.cfg.cli.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileValidationResult {
    String path;
    String status;
    String error;
}
