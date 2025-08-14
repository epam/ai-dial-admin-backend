package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LimitDtoMapper.class, RoleBasedDtoMapper.class, InstantMapper.class, FeaturesMapper.class})
public interface ApplicationDtoMapper {

    @Mapping(target = "descriptionKeywords", source = "topics")
    @Mapping(target = "applicationTypeSchemaId", source = "customAppSchemaId")
    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Application toDomain(ApplicationDto dto);

    @Mapping(target = "function", ignore = true)
    @Mapping(target = "topics", source = "descriptionKeywords")
    @Mapping(target = "customAppSchemaId", source = "applicationTypeSchemaId")
    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    ApplicationDto toDto(Application domain);

    @Mapping(target = "topics", source = "descriptionKeywords")
    @Mapping(target = "name", source = "deployment.name")
    ApplicationInfoDto toApplicationInfoDto(Application application);

}
