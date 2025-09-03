package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import com.epam.aidial.cfg.dto.AssistantsPropertyDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = FeaturesDtoMapper.class)
public interface AssistantsPropertyDtoMapper {

    AssistantsProperty toDomain(AssistantsPropertyDto entity);

    AssistantsPropertyDto toDto(AssistantsProperty domain);
}
