package com.epam.aidial.cfg.domain.model.route;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Route extends BaseRoute {
    private String displayName;
    private Set<String> topics;
}