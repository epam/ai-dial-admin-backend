package com.epam.aidial.cfg.service.normalizer.impl;

import com.epam.aidial.cfg.service.normalizer.CoreConfigNormalizer;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class KeyNormalizer implements CoreConfigNormalizer {

    @Override
    public void normalize(Config config) {
        Map<String, CoreKey> keyMap = config.getKeys();
        if (MapUtils.isEmpty(keyMap)) {
            return;
        }
        Map<String, CoreKey> keys = config.getKeys();
        for (Map.Entry<String, CoreKey> keyId2ValuePair : new HashMap<>(keys).entrySet()) {
            String keyId = keyId2ValuePair.getKey();
            CoreKey key = keyId2ValuePair.getValue();
            if (StringUtils.isEmpty(key.getRole()) && CollectionUtils.isEmpty(key.getRoles())) {
                log.debug("normalizeConfig. remove invalid key: {}", keyId);
                keys.remove(keyId);
            }
        }
    }
}