package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.dto.AssistantDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;

@Mapper(componentModel = "spring", uses = {LimitDtoMapper.class, RoleBasedDtoMapper.class})
public interface AssistantDtoMapper {

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLong")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToLong")
    Assistant toDomain(AssistantDto entity);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "defaults", source = "defaults", qualifiedByName = "mapDefaults")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    AssistantDto toDto(Assistant domain);

    @Named("instantToLong")
    static Long mapInstantToLong(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }

    @Named("longToInstant")
    static Instant mapLongToInstant(Long epochMilli) {
        return epochMilli != null ? Instant.ofEpochMilli(epochMilli) : null;
    }
}
