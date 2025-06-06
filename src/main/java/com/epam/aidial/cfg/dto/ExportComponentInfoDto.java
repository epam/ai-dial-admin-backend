package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportComponentInfoDto {

    private String name;
    private String displayName;
    private String displayVersion;
    private String description;
    private ExportConfigComponentType type;
}
