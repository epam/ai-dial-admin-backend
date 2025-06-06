package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.dto.KeyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KeyDtoMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "keyGeneratedAt", ignore = true)
    Key toDomain(KeyDto entity);

    KeyDto toDto(Key domain);

    @Mapping(target = "key", ignore = true)
    KeyDto toDtoWithoutKey(Key domain);
}
