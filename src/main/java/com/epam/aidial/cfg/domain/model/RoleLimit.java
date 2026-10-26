package com.epam.aidial.cfg.domain.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder(toBuilder = true)
public class RoleLimit {

    private String role;
    private String deploymentName;

    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private boolean enabled = true;
    private Limit limit;
}
