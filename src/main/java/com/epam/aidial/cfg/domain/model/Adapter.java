package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Adapter {

    private String name;
    private String displayName;
    private String baseEndpoint;
    private String description;
    private List<String> models = new ArrayList<>();
}
