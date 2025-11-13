package com.epam.aidial.cfg.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.concurrent.TimeUnit;

@Mapper(componentModel = "spring")
public abstract class TimeMapper {

    private static final long MS_IN_HOUR = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);

    @Named("msToHours")
    public Double msToHours(Long milliseconds) {
        if (milliseconds == null) {
            return null;
        }
        return ((double) milliseconds) / MS_IN_HOUR;
    }

    @Named("msToHoursWithTruncation")
    public Long msToHoursWithTruncation(Long milliseconds) {
        Double hours = msToHours(milliseconds);
        if (hours == null) {
            return null;
        }
        return hours.longValue();
    }

    @Named("hoursToMs")
    public Long hoursToMs(Long hours) {
        if (hours == null) {
            return null;
        }
        return hours * MS_IN_HOUR;
    }
}
