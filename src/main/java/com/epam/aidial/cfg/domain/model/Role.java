package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.model.ResourceType;
import lombok.Data;

import java.util.List;
import java.util.Map;

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
    private Map<ResourceType, ShareResourceLimit> share;
}
