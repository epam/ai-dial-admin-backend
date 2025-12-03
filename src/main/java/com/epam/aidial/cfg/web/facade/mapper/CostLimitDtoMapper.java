package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.CostLimit;
import com.epam.aidial.cfg.dto.CostLimitDto;
import com.epam.aidial.cfg.utils.NullSafeUtils;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CostLimitDtoMapper {

    CostLimitDto toDto(CostLimit domain);

    default CostLimit toDomain(CostLimitDto dto) {
        CostLimit costLimit = new CostLimit();

        if (dto != null) {
            NullSafeUtils.setIfNotNull(costLimit::setMinute, dto.getMinute());
            NullSafeUtils.setIfNotNull(costLimit::setDay, dto.getDay());
            NullSafeUtils.setIfNotNull(costLimit::setWeek, dto.getWeek());
            NullSafeUtils.setIfNotNull(costLimit::setMonth, dto.getMonth());
        }

        return costLimit;
    }
}
