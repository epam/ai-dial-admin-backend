package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.core.config.CoreShareResourceLimit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoleShareResourceLimitMapperTest {

    private RoleShareResourceLimitMapperImpl roleShareResourceLimitMapper;

    @BeforeEach
    void setUp() {
        roleShareResourceLimitMapper = new RoleShareResourceLimitMapperImpl();
    }

    @Test
    void testMapShareResourceLimits() throws JsonProcessingException {
        // given
        ObjectMapper objectMapper = new ObjectMapper();

        List<RoleShareResourceLimit> roleShareResourceLimits = objectMapper.readValue(
                ResourceUtils.readResource("/mapper/core/role_share_resource_limit/map_share_resource_limits/role_share_resource_limits.json"),
                new TypeReference<>() {
                }
        );
        List<Deployment> deployments = objectMapper.readValue(
                ResourceUtils.readResource("/mapper/core/role_share_resource_limit/map_share_resource_limits/deployments.json"),
                new TypeReference<>() {
                }
        );

        Map<String, CoreShareResourceLimit> expectedResult = objectMapper.readValue(
                ResourceUtils.readResource("/mapper/core/role_share_resource_limit/map_share_resource_limits/core_share_resource_limits.json"),
                new TypeReference<>() {
                }
        );

        // when
        Map<String, CoreShareResourceLimit> actualResult = roleShareResourceLimitMapper.mapShareResourceLimits(roleShareResourceLimits, deployments);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}