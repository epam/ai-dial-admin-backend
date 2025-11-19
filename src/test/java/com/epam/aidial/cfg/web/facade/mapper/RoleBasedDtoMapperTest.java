package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.dto.LimitDto;
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
        RoleBasedDtoMapperImpl.class,
        LimitDtoMapperImpl.class
})
class RoleBasedDtoMapperTest {

    @Autowired
    private RoleBasedDtoMapper mapper;

    @Test
    void testMapRoleLimitsToDomain() {
        // given
        LimitDto limitDto1 = new LimitDto();

        LimitDto limitDto2 = new LimitDto();
        limitDto2.setDay(10L);

        LimitDto limitDto3 = new LimitDto();
        limitDto3.setEnabled(false);
        limitDto3.setDay(10L);

        Map<String, LimitDto> limits = Map.of(
                "role1", limitDto1,
                "role2", limitDto2,
                "role3", limitDto3
        );

        Limit expectedLimit1 = new Limit();
        RoleLimit expectedRoleLimit1 = new RoleLimit();
        expectedRoleLimit1.setRole("role1");
        expectedRoleLimit1.setLimit(expectedLimit1);

        Limit expectedLimit2 = new Limit();
        expectedLimit2.setDay(10L);
        RoleLimit expectedRoleLimit2 = new RoleLimit();
        expectedRoleLimit2.setRole("role2");
        expectedRoleLimit2.setLimit(expectedLimit2);

        Limit expectedLimit3 = new Limit();
        expectedLimit3.setDay(10L);
        RoleLimit expectedRoleLimit3 = new RoleLimit();
        expectedRoleLimit3.setRole("role3");
        expectedRoleLimit3.setEnabled(false);
        expectedRoleLimit3.setLimit(expectedLimit3);

        List<RoleLimit> expected = List.of(expectedRoleLimit1, expectedRoleLimit2, expectedRoleLimit3);

        // when
        List<RoleLimit> actual = mapper.mapRoleLimitsToDomain(limits);

        // then
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

}