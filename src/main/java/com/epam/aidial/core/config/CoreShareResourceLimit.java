package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreShareResourceLimit {

    @JsonAlias({"maxAcceptedUsers", "max_accepted_users"})
    Integer maxAcceptedUsers = -1;
    @JsonAlias({"invitationTtl", "invitation_ttl"})
    Long invitationTtl = -1L;

    public CoreShareResourceLimit() {

    }

    public CoreShareResourceLimit(int maxAcceptedUsers, long invitationTtl) {
        this.maxAcceptedUsers = maxAcceptedUsers;
        this.invitationTtl = invitationTtl;
    }

}