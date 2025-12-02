package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.client.mapper.RouteMapper;
import com.epam.aidial.cfg.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.ApplicationResourceNodeInfoDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.dto.CreateApplicationResourceDto;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.ApplicationResourceNodeInfo;
import com.epam.aidial.cfg.model.ApplicationsExim;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RouteMapper.class})
public interface ApplicationResourceMapper {

    CreateApplicationResource toCreateApplicationResourceDto(CreateApplicationResourceDto createApplicationResourceDto);

    ApplicationResourceNodeInfoDto toApplicationResourceNodeInfoDto(ApplicationResourceNodeInfo applicationResourceNodeInfo);

    ApplicationResourceDto toApplicationResourceDto(ApplicationResource model);

    ApplicationsEximDto toApplicationsEximDto(ApplicationsExim model);
}