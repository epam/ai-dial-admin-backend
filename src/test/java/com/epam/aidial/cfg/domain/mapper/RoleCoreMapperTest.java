package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

class RoleCoreMapperTest {

    @Test
    void mapRole() {
        // given
        RoleCoreMapper mapper = Mappers.getMapper(RoleCoreMapper.class);
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