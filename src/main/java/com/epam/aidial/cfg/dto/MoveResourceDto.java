package com.epam.aidial.cfg.dto;


import com.epam.aidial.cfg.dto.validation.annotation.MetadataPath;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveResourceDto {

    @NotNull
    @MetadataPath
    private String sourceUrl;
    @NotNull
    @MetadataPath
    private String destinationUrl;
    private boolean overwrite;

}
