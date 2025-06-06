package com.epam.aidial.cfg.service.normalizer.impl;

import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreKey;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class KeyNormalizerTests {

    private KeyNormalizer normalizer;

    @BeforeEach
    void init() {
        normalizer = new KeyNormalizer();
    }

    @Test
    void testNormalize_RoleIsMissing_RemoveKey() {
        // given
        Config config = new Config();
        CoreKey key = new CoreKey();
        Map<String, CoreKey> keysMap = new HashMap<>();
        keysMap.put("testKey", key);
        config.setKeys(keysMap);
        // when
        normalizer.normalize(config);
        // then
        Assertions.assertThat(config.getKeys()).isEmpty();
    }

    @Test
    void testNormalize_RoleIsNotMissing_DoesNotRemoveKey() {
        // given
        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setRole("default");
        Map<String, CoreKey> keysMap = new HashMap<>();
        keysMap.put("testKey", key);
        config.setKeys(keysMap);
        // when
        normalizer.normalize(config);
        // then
        Assertions.assertThat(config.getKeys()).hasSize(1)
                .satisfies(keys -> {
                    CoreKey testKey = keys.get("testKey");
                    Assertions.assertThat(testKey).isNotNull();
                });
    }
}