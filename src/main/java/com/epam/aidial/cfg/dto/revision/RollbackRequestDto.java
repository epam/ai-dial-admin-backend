package com.epam.aidial.cfg.dto.revision;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RollbackRequestDto {
    @NotNull
    @Positive
    protected Integer revisionNumber;
}

