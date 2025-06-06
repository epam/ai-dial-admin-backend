package com.epam.aidial.core.config;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CoreAssistant extends Deployment {
    private String prompt;
    private List<String> addons = List.of();
}