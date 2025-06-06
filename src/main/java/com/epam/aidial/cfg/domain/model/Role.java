package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class Role {
    private String name;
    private String description;
    private List<RoleLimit> limits;
    private List<String> keys;
}
