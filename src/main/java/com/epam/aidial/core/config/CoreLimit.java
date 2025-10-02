package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class CoreLimit {
    private Long minute = Long.MAX_VALUE;
    private Long day = Long.MAX_VALUE;
    private Long week = Long.MAX_VALUE;
    private Long month = Long.MAX_VALUE;
    private Long requestHour = Long.MAX_VALUE;
    private Long requestDay = Long.MAX_VALUE;

    @JsonIgnore
    public boolean isPositive() {
        return minute > 0 && day > 0 && week > 0 && month > 0 && requestDay > 0 && requestHour > 0;
    }

    @JsonIgnore
    public static CoreLimit empty() {
        CoreLimit coreLimit = new CoreLimit();

        coreLimit.setMinute(null);
        coreLimit.setDay(null);
        coreLimit.setWeek(null);
        coreLimit.setMonth(null);
        coreLimit.setRequestHour(null);
        coreLimit.setRequestDay(null);

        return coreLimit;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return minute == null && day == null && week == null && month == null && requestDay == null && requestHour == null;
    }
}
