package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LimitDtoMapper.class, RoleBasedDtoMapper.class})
public interface ApplicationDtoMapper {

    @Mapping(target = "createdAt", source = "createdAtMs")
    @Mapping(target = "updatedAt", source = "updatedAtMs")
    @Mapping(target = "descriptionKeywords", source = "topics")
    @Mapping(target = "applicationTypeSchemaId", source = "customAppSchemaId")
    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    Application toDomain(ApplicationDto dto);

    @Mapping(target = "createdAtMs", source = "createdAt")
    @Mapping(target = "updatedAtMs", source = "updatedAt")
    @Mapping(target = "function", ignore = true)
    @Mapping(target = "topics", source = "descriptionKeywords")
    @Mapping(target = "customAppSchemaId", source = "applicationTypeSchemaId")
    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    ApplicationDto toDto(Application domain);

    @Mapping(target = "createdAtMs", source = "createdAt")
    @Mapping(target = "updatedAtMs", source = "updatedAt")
    @Mapping(target = "topics", source = "descriptionKeywords")
    @Mapping(target = "name", source = "deployment.name")
    ApplicationInfoDto toApplicationInfoDto(Application application);

}
