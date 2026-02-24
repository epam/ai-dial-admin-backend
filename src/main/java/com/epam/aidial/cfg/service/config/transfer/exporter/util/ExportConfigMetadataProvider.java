package com.epam.aidial.cfg.service.config.transfer.exporter.util;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentMetadata;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportConfigMetadata;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@LogExecution
public class ExportConfigMetadataProvider {

    public ExportConfigMetadata getMetadata(ExportFormat exportFormat) {
        var components = getAllMetadata(exportFormat);

        return ExportConfigMetadata.builder()
                .components(components)
                .build();
    }

    private List<ExportConfigComponentMetadata> getAllMetadata(ExportFormat exportFormat) {
        return Arrays.stream(ExportConfigComponentType.values())
                .filter(type -> type.supports(exportFormat))
                .map(type -> new ExportConfigComponentMetadata(type, type.getAllDependencies(exportFormat)))
                .toList();
    }
}
