package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class Adapter {

    private String name;
    private String displayName;
    private String baseEndpoint;
    private String responsesEndpoint;
    private String description;
    private Long createdAt;
    private Long updatedAt;
    private List<String> models = new ArrayList<>();
    private Set<String> topics;
    private AdapterSource source;
}