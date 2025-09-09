package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.core.config.CoreLimit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testMapLimits() throws JsonProcessingException {
        // given
        ObjectMapper objectMapper = new ObjectMapper();

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