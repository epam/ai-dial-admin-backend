package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.dto.AdapterDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdapterDtoMapper {

    @Mapping(target = "models", ignore = true)
    Adapter toDomain(AdapterDto entity);

    AdapterDto toDto(Adapter domain);
}
