package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.model.EntitySyncState;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntitySyncStateDtoMapper {

    EntitySyncStateDto toDto(EntitySyncState entitySyncState);
}
