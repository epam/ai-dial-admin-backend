package com.epam.aidial.cfg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportComponentInfo {

    private String name;
    private LocalizedValue displayName;
    private String displayVersion;
    private LocalizedValue description;
    private ExportConfigComponentType type;
}
