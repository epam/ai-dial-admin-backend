package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.core.config.Config;

public record ConfigPart(Config config, String encoded) {
}
