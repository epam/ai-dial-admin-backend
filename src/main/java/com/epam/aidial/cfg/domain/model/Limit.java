package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class Limit {
    private Long minute;
    private Long day;
    private Long week;
    private Long month;
    private Long requestHour;
    private Long requestDay;
}
