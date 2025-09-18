package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class Role {
    private String name;
    private String description;
    private String displayName;
    private Long createdAt;
    private Long updatedAt;
    private List<RoleLimit> limits;
    private List<String> keys;
    private CostLimit costLimit;
    private List<RoleShareResourceLimit> share;
}
