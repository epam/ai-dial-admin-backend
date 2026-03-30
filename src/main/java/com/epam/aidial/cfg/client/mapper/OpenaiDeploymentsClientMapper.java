package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.model.ToolSetData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OpenaiDeploymentsClientMapper {

    ToolSetData toToolSetData(ToolSetDataDto dto);
}