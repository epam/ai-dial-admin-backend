package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record ConfigMergeResult(Config config, List<String> warnings, JsonNode rawMergedNode) {}
