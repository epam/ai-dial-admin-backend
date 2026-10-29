package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreShareResourceLimit {

    @JsonAlias({"maxAcceptedUsers", "max_accepted_users"})
    Integer maxAcceptedUsers = -1;
    @JsonAlias({"invitationTtl", "invitation_ttl"})
    Long invitationTtl = -1L;

}