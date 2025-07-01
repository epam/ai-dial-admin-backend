package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;

@Mapper(componentModel = "spring", uses = {LimitDtoMapper.class, RoleBasedDtoMapper.class})
public interface ApplicationDtoMapper {

    @Mapping(target = "descriptionKeywords", source = "topics")
    @Mapping(target = "applicationTypeSchemaId", source = "customAppSchemaId")
    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLong")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToLong")
    Application toDomain(ApplicationDto dto);

    @Mapping(target = "function", ignore = true)
    @Mapping(target = "topics", source = "descriptionKeywords")
    @Mapping(target = "customAppSchemaId", source = "applicationTypeSchemaId")
    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    ApplicationDto toDto(Application domain);

    @Mapping(target = "topics", source = "descriptionKeywords")
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    ApplicationInfoDto toApplicationInfoDto(Application application);

    @Named("instantToLong")
    static Long mapInstantToLong(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }

    @Named("longToInstant")
    static Instant mapLongToInstant(Long epochMilli) {
        return epochMilli != null ? Instant.ofEpochMilli(epochMilli) : null;
    }
}
