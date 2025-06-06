package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * Created by Aliaksei Kurnosau on 9/11/24.
 */
@Data
public class LimitDto {

    private boolean enabled = true;

    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Long minute;

    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Long day;
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Long week;
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Long month;
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Long requestHour;
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Long requestDay;
}
