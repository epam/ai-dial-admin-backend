package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.client.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.client.dto.ResourceSignOutRequestDto;
import com.epam.aidial.cfg.dto.CreateToolSetResourceDto;
import com.epam.aidial.cfg.dto.ToolSetResourceDto;
import com.epam.aidial.cfg.dto.ToolSetResourceNodeInfoDto;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResourceNodeInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ToolSetResourceMapper {

    CreateToolSetResource toCreateToolSetResourceDto(CreateToolSetResourceDto createToolSetResourceDto);

    ToolSetResourceNodeInfoDto toToolSetResourceNodeInfoDto(ToolSetResourceNodeInfo toolSetResourceNodeInfo);

    ToolSetResourceDto toToolSetResourceDto(ToolSetResource model);

    ResourceSignInRequestDto toResourceSignInRequest(com.epam.aidial.cfg.dto.ResourceSignInRequestDto dto);

    ResourceSignOutRequestDto toResourceSignOutRequest(com.epam.aidial.cfg.dto.ResourceSignOutRequestDto dto);
}