package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SelectedItemsExportRequestDto.class, name = "custom"),
        @JsonSubTypes.Type(value = FullExportRequestDto.class, name = "full")
})
public abstract class ExportRequestDto {

    @NotNull
    private ExportFormatDto exportFormat;
    private boolean addSecrets;

}
