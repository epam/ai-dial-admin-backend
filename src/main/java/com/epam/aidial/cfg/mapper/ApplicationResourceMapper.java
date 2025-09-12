package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.client.mapper.RouteMapper;
import com.epam.aidial.cfg.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.ApplicationResourceNodeInfoDto;
import com.epam.aidial.cfg.dto.CreateApplicationResourceDto;
import com.epam.aidial.cfg.model.ApplicationNodeInfo;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RouteMapper.class})
public interface ApplicationResourceMapper {

    CreateApplicationResource toCreateApplicationResourceDto(CreateApplicationResourceDto createApplicationResourceDto);

    ApplicationResourceNodeInfoDto toApplicationResourceNodeInfoDto(ApplicationNodeInfo applicationNodeInfo);

    ApplicationResourceDto toApplicationResourceDto(ApplicationResource model);
}
