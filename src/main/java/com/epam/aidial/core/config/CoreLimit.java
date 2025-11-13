package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.OmitLongMaxValueFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class CoreLimit {
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitLongMaxValueFilter.class)
    private Long minute = Long.MAX_VALUE;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitLongMaxValueFilter.class)
    private Long day = Long.MAX_VALUE;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitLongMaxValueFilter.class)
    private Long week = Long.MAX_VALUE;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitLongMaxValueFilter.class)
    private Long month = Long.MAX_VALUE;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitLongMaxValueFilter.class)
    private Long requestHour = Long.MAX_VALUE;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitLongMaxValueFilter.class)
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
