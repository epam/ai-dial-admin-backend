package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AssistantsPropertyEntity;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class AssistantsPropertyEntityMapper {

    public abstract AssistantsProperty toDomain(AssistantsPropertyEntity entity);

    @Mapping(target = "id", ignore = true)
    public abstract AssistantsPropertyEntity toEntity(AssistantsProperty domain, @MappingTarget AssistantsPropertyEntity entity);
}
