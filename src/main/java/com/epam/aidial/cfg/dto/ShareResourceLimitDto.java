package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class ShareResourceLimitDto {
    private Integer maxAcceptedUsers;
    private Integer invitationTtl;
}
