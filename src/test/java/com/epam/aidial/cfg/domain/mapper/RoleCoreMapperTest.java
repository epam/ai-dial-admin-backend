package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RoleLimitMapperImpl.class,
        RoleShareResourceLimitMapperImpl.class,
        RoleCoreMapperImpl.class
})
class RoleCoreMapperTest {

    @Autowired
    private RoleCoreMapper mapper;

    @Test
    void mapRole() {
        // given
        Role role = new Role();
        role.setName("testRole");
        Limit limit = new Limit();
        RoleLimit roleLimit = new RoleLimit();
        roleLimit.setDeploymentName("testModel");
        roleLimit.setLimit(limit);
        role.setLimits(List.of(roleLimit));
        // when
        CoreRole result = mapper.mapRole(role, List.of());
        // then
        Assertions.assertThat(result).isNotNull().satisfies(coreRole -> {
            Assertions.assertThat(coreRole.getName()).isEqualTo("testRole");
            Assertions.assertThat(coreRole.getLimits()).isEmpty();
        });
    }
}