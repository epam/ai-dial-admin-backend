package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class GlobalSettings {

    private List<String> globalInterceptors;
}