package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Data
public class ShareResourceLimitDto {
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer maxAcceptedUsers;
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private Long invitationTtl;
}
