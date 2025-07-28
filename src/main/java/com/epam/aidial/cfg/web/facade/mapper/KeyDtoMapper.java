package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.dto.KeyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = InstantMapper.class)
public interface KeyDtoMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "keyGeneratedAt", ignore = true)
    @Mapping(target = "expiresAt", source = "expiresAt", qualifiedByName = "instantToLong")
    Key toDomain(KeyDto entity);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    @Mapping(target = "expiresAt", source = "expiresAt", qualifiedByName = "longToInstant")
    @Mapping(target = "keyGeneratedAt", source = "keyGeneratedAt", qualifiedByName = "longToInstant")
    KeyDto toDto(Key domain);

    @Mapping(target = "key", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    @Mapping(target = "expiresAt", source = "expiresAt", qualifiedByName = "longToInstant")
    @Mapping(target = "keyGeneratedAt", source = "keyGeneratedAt", qualifiedByName = "longToInstant")
    KeyDto toDtoWithoutKey(Key domain);
}
