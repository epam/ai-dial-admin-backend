package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.ExportApplicationTypeSchemaInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigPreview;
import com.epam.aidial.cfg.domain.model.ExportKeyInfo;
import com.epam.aidial.cfg.dto.ExportApplicationTypeSchemaInfoDto;
import com.epam.aidial.cfg.dto.ExportConfigPreviewDto;
import com.epam.aidial.cfg.dto.ExportKeyInfoDto;
import com.epam.aidial.cfg.dto.ExportRequestDto;
import com.epam.aidial.cfg.dto.FullExportRequestDto;
import com.epam.aidial.cfg.dto.SelectedItemsExportRequestDto;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassMapping;

import static org.mapstruct.SubclassExhaustiveStrategy.RUNTIME_EXCEPTION;

@Mapper(componentModel = "spring")
public interface ExportConfigMapper {

    @SubclassMapping(source = FullExportRequestDto.class, target = FullExportRequest.class)
    @SubclassMapping(source = SelectedItemsExportRequestDto.class, target = SelectedItemsExportRequest.class)
    @BeanMapping(subclassExhaustiveStrategy = RUNTIME_EXCEPTION)
    ExportRequest toExportRequest(ExportRequestDto dto);

    ExportConfigPreviewDto toExportConfigPreviewDto(ExportConfigPreview preview);

    ExportKeyInfoDto toExportKeyInfoDto(ExportKeyInfo exportKeyInfo);

    ExportApplicationTypeSchemaInfoDto toExportApplicationTypeSchemaInfoDto(ExportApplicationTypeSchemaInfo typeSchemaInfo);
}
