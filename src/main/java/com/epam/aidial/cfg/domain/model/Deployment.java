package com.epam.aidial.cfg.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(exclude = "roleLimits")
@NoArgsConstructor(access = AccessLevel.PACKAGE) // for tests
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type",
        defaultImpl = Deployment.class
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SecuredResource.class, name = "SecuredResource")
})
public class Deployment {
    private String name;
    private List<RoleLimit> roleLimits;
    private List<RoleShareResourceLimit> roleShareResourceLimits;
    private Boolean isPublic = false;
    private Limit defaultRoleLimit;
    private ShareResourceLimit defaultRoleShareResourceLimit;

    public Deployment(String name) {
        this.name = name;
    }
}
