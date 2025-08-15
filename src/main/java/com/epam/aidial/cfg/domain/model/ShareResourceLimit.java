package com.epam.aidial.cfg.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ShareResourceLimit {

    private Integer maxAcceptedUsers;
    private Long invitationTtl;

    @JsonIgnore
    public boolean isEmpty() {
        return maxAcceptedUsers == null && invitationTtl == null;
    }
}
