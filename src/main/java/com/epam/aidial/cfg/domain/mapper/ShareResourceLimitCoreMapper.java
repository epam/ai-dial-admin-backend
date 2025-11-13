package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.core.config.CoreShareResourceLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TimeMapper.class})
public interface ShareResourceLimitCoreMapper {

    @Mapping(target = "invitationTtl", source = "invitationTtl", qualifiedByName = "hoursToMs")
    ShareResourceLimit toShareResourceLimit(CoreShareResourceLimit limit);

    @Mapping(target = "invitationTtl", source = "invitationTtl", qualifiedByName = "msToHoursWithTruncation")
    CoreShareResourceLimit toCoreShareResourceLimit(ShareResourceLimit limit);
}
