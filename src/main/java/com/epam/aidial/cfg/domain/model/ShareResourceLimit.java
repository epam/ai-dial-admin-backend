package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class ShareResourceLimit {

    private Integer maxAcceptedUsers;
    private Integer invitationTtl;
}
