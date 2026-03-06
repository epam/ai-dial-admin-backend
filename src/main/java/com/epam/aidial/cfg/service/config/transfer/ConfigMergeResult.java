package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.core.config.Config;

import java.util.List;

public record ConfigMergeResult(Config config, List<String> warnings) {}
