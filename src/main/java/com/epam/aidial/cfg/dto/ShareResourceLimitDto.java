package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ShareResourceLimitDto {
    @PositiveOrZero
    private Integer maxAcceptedUsers;
    @PositiveOrZero
    private Long invitationTtl;
}
