package com.epam.aidial.cfg.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Limit {
    private Long minute;
    private Long day;
    private Long week;
    private Long month;
    private Long requestHour;
    private Long requestDay;

    @JsonIgnore
    public boolean isEmpty() {
        return minute == null && day == null && week == null && month == null && requestHour == null && requestDay == null;
    }
}
