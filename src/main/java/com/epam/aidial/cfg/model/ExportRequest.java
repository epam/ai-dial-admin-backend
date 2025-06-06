package com.epam.aidial.cfg.model;

import com.epam.aidial.cfg.domain.model.ExportFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ExportRequest {

    private ExportFormat exportFormat;
    private boolean addSecrets;
}
