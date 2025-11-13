package com.epam.aidial.cfg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RoleLimit {

    private String role;
    private String deploymentName;

    @Builder.Default
    private boolean enabled = true;
    private Limit limit;
}
