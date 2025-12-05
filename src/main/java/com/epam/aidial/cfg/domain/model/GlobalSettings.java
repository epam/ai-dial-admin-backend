package com.epam.aidial.cfg.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class GlobalSettings {

    private List<String> globalInterceptors;

    @JsonIgnore
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(globalInterceptors);
    }
}