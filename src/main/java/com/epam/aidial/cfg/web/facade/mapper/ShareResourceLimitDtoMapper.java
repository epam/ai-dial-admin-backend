package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShareResourceLimitDtoMapper {

    ShareResourceLimit toShareResourceLimit(ShareResourceLimitDto dto);

    ShareResourceLimitDto toShareResourceLimitDto(ShareResourceLimit model);
}
