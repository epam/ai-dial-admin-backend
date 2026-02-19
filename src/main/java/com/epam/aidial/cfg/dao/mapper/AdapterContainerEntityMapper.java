package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AdapterContainerEntity;
import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdapterContainerEntityMapper {

    AdapterContainerEntity toEntity(AdapterContainerSource domain);

    AdapterContainerSource toDomain(AdapterContainerEntity entity);
}
