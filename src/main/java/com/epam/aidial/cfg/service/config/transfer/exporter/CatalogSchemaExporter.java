package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.domain.model.ExportCatalogSchemaInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.service.CatalogSchemaService;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CatalogSchemaExporter {

    private final CatalogSchemaService catalogSchemaService;

    public Map<String, CatalogSchema> getCatalogSchemas(ExportRequest request) {
        if (request instanceof FullExportRequest) {
            return catalogSchemaService.getAll()
                    .stream()
                    .collect(Collectors.toMap(CatalogSchema::getSchemaId, Function.identity()));
        }

        if (request instanceof SelectedItemsExportRequest selectedRequest) {
            return selectedRequest.getComponents()
                    .stream()
                    .filter(component -> component.getType() == ExportConfigComponentType.CATALOG_SCHEMA)
                    .map(component -> catalogSchemaService.get(component.getName()))
                    .collect(Collectors.toMap(CatalogSchema::getSchemaId, Function.identity()));
        }

        return Map.of();
    }

    protected Collection<ExportCatalogSchemaInfo> preview(ExportRequest request) {
        return getCatalogSchemas(request).values().stream()
                .map(component -> ExportCatalogSchemaInfo.builder()
                        .id(component.getSchemaId())
                        .displayName(component.getCatalogDisplayName())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.CATALOG_SCHEMA)
                        .build())
                .collect(Collectors.toList());
    }
}
