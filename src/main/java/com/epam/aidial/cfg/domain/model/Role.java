package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.model.ResourceType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class Role {
    private String name;
    private String description;
    private String displayName;
    private Long createdAt;
    private Long updatedAt;
    private List<RoleLimit> limits;
    private List<String> keys;
    private CostLimit costLimit = new CostLimit();
    private Map<ResourceType, ShareResourceLimit> share;
    private Set<String> topics;
}