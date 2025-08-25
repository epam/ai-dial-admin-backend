package com.epam.aidial.cfg.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ShareResourceLimit {

    private Integer maxAcceptedUsers;
    private Long invitationTtl = 259200L; // seconds in 72 hours

    @JsonIgnore
    public boolean isEmpty() {
        return maxAcceptedUsers == null && invitationTtl == null;
    }
}
