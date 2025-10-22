package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RoleLimitMapperImpl.class,
        ShareResourceLimitCoreMapperImpl.class,
        RoleCoreMapperImpl.class,
        CostLimitCoreMapperImpl.class,
        TimeMapperImpl.class
})
class RoleCoreMapperTest {

    @Autowired
    private RoleCoreMapper mapper;

    @Test
    void mapRole() {
        // given
        Limit limit = new Limit();
        limit.setDay(10L);

        RoleLimit roleLimit = new RoleLimit();
        roleLimit.setRole("testRole");
        roleLimit.setDeploymentName("testModel");
        roleLimit.setLimit(limit);

        Role role = new Role();
        role.setName("testRole");
        role.setLimits(List.of(roleLimit));

        Deployment deployment = new Deployment("testModel");
        deployment.setDefaultRoleLimit(new Limit());
        deployment.setRoleLimits(List.of(roleLimit));

        CoreLimit expectedLimit = CoreLimit.empty();
        expectedLimit.setDay(10L);

        CoreRole expected = new CoreRole();
        expected.setName("testRole");
        expected.setLimits(Map.of("testModel", expectedLimit));

        // when
        CoreRole result = mapper.mapRole(role, List.of(deployment));

        // then
        Assertions.assertThat(result).isEqualTo(expected);
    }
}