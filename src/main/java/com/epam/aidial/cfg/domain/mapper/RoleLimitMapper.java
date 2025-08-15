package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleLimitMapper {

    @Mapping(target = "role", ignore = true)
    RoleLimit toLimit(CoreLimit limit, String deploymentName, boolean enabled);

    default Limit toLimit(CoreLimit limit) {
        if (limit == null) {
            return null;
        }

        Limit domainLimit = new Limit();

        domainLimit.setMinute(getLimit(limit.getMinute()));
        domainLimit.setDay(getLimit(limit.getDay()));
        domainLimit.setWeek(getLimit(limit.getWeek()));
        domainLimit.setMonth(getLimit(limit.getMonth()));
        domainLimit.setRequestHour(getLimit(limit.getRequestHour()));
        domainLimit.setRequestDay(getLimit(limit.getRequestDay()));

        return domainLimit;
    }

    private Long getLimit(long value) {
        return Long.MAX_VALUE == value ? null : value;
    }

}
