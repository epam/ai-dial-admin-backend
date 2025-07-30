package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.dto.AdapterDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = InstantMapper.class)
public interface AdapterDtoMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Adapter toDomain(AdapterDto entity);

    AdapterDto toDto(Adapter domain);
}
