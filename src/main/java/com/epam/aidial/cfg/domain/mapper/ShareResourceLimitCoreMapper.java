package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.core.config.CoreShareResourceLimit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShareResourceLimitCoreMapper {

    ShareResourceLimit toShareResourceLimit(CoreShareResourceLimit limit);

    CoreShareResourceLimit toCoreShareResourceLimit(ShareResourceLimit limit);

}
