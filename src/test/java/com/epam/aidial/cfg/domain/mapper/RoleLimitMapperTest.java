package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.core.config.CoreLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoleLimitMapperTest {

    private RoleLimitMapperImpl roleLimitMapper;

    @BeforeEach
    void setUp() {
        roleLimitMapper = new RoleLimitMapperImpl();
    }

    @Test
    void testMapLimits() throws JacksonException {
        // given
        ObjectMapper objectMapper = JsonMapperConfiguration.withLegacyCompatibility(JsonMapper.builder()).build();

        List<RoleLimit> roleLimits = objectMapper.readValue(
                ResourceUtils.readResource("/mapper/core/role_limit/map_limits/role_limits.json"),
                new TypeReference<>() {
                }
        );
        List<Deployment> deployments = objectMapper.readValue(
                ResourceUtils.readResource("/mapper/core/role_limit/map_limits/deployments.json"),
                new TypeReference<>() {
                }
        );

        Map<String, CoreLimit> expectedResult = objectMapper.readValue(
                ResourceUtils.readResource("/mapper/core/role_limit/map_limits/core_limits.json"),
                new TypeReference<>() {
                }
        );

        // when
        Map<String, CoreLimit> actualResult = roleLimitMapper.mapLimits(roleLimits, deployments);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }
}