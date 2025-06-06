package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class LimitEntity {

    @Column(name = "default_minute")
    private Long minute;
    @Column(name = "default_day")
    private Long day;
    @Column(name = "default_week")
    private Long week;
    @Column(name = "default_month")
    private Long month;
    @Column(name = "default_request_hour")
    private Long requestHour;
    @Column(name = "default_request_day")
    private Long requestDay;
}
