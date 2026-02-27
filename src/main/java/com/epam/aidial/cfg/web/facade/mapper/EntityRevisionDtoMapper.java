package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.EntityRevision;
import com.epam.aidial.cfg.dto.EntityRevisionDto;
import org.mapstruct.Mapper;

import java.util.function.Function;

@Mapper(componentModel = "spring")
public interface EntityRevisionDtoMapper {

    default <T, I> EntityRevisionDto<I> toDto(EntityRevision<T> entityRevision, Function<T, I> modelToDtoMapper) {
        return EntityRevisionDto.<I>builder()
                .state(modelToDtoMapper.apply(entityRevision.getState()))
                .configRevisionId(entityRevision.getConfigRevisionId())
                .revisionType(toDto(entityRevision.getRevisionType()))
                .build();
    }

    EntityRevisionDto.RevisionTypeDto toDto(EntityRevision.RevisionType revisionType);

}
