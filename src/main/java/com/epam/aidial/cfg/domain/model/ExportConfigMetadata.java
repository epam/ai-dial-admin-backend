package com.epam.aidial.cfg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExportConfigMetadata {

    private List<ExportConfigComponentMetadata> components;
}
