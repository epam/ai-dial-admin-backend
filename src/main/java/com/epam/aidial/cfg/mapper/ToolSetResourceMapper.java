package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.CreateToolSetResourceDto;
import com.epam.aidial.cfg.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.dto.ResourceSignOutRequestDto;
import com.epam.aidial.cfg.dto.ToolSetResourceDto;
import com.epam.aidial.cfg.dto.ToolSetResourceNodeInfoDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.ResourceSignInRequest;
import com.epam.aidial.cfg.model.ResourceSignOutRequest;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResourceNodeInfo;
import com.epam.aidial.cfg.model.ToolSetsExim;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ToolSetResourceMapper {

    CreateToolSetResource toCreateToolSetResourceDto(CreateToolSetResourceDto createToolSetResourceDto);

    ToolSetResourceNodeInfoDto toToolSetResourceNodeInfoDto(ToolSetResourceNodeInfo toolSetResourceNodeInfo);

    ToolSetResourceDto toToolSetResourceDto(ToolSetResource model);

    ResourceSignInRequest toResourceSignInRequest(ResourceSignInRequestDto dto);

    ResourceSignOutRequest toResourceSignOutRequest(ResourceSignOutRequestDto dto);

    ToolSetsEximDto toToolSetsEximDto(ToolSetsExim model);
}