package com.epam.aidial.cfg.dto;


import com.epam.aidial.cfg.web.validation.PublicPath;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImportResourcesDto {

    @PublicPath
    private String path;
    @NotNull
    private ImportResourcesConflictResolutionStrategyDto conflictResolutionStrategy;

}
