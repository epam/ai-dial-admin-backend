package com.epam.aidial.cfg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExportConfigComponentMetadata {

    private ExportConfigComponentType type;
    private Set<ExportConfigComponentType> dependencies;
}
