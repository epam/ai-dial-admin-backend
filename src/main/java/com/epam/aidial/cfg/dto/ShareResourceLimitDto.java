package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ShareResourceLimitDto {
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer maxAcceptedUsers;
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Long invitationTtl = 259200L; // seconds in 72 hours
}
