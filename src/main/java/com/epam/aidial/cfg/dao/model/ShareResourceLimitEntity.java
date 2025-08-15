package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ShareResourceLimitEntity {

    private Integer maxAcceptedUsers;
    private Long invitationTtl;
}
