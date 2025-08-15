package com.epam.aidial.cfg.service.transfer.importer.util;

import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapperImpl;
import com.epam.aidial.cfg.domain.mapper.RoleLimitMapperImpl;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RoleLimitMapperImpl.class,
        RoleCoreMapperImpl.class
})
class CoreRolesMergerTest {

    @Autowired
    private RoleCoreMapper roleCoreMapper;

    @MockitoBean
    private RoleService roleService;

    private CoreRolesMerger coreRolesMerger;

    @BeforeEach
    void setUp() {
        coreRolesMerger = new CoreRolesMerger(roleService, roleCoreMapper);
    }

    @Test
    void mergeCoreRoles_singleModelWithUserRolesAndRoles_shouldMergeCoreRoles() throws JsonProcessingException {
        // given
        ObjectMapper objectMapper = new ObjectMapper();

        Config config = objectMapper.readValue(
                ResourceUtils.readResource("/import/core_roles_merge/single_model_with_user_roles_and_roles/config.json"),
                Config.class
        );
        Map<String, Role> expectedResult = objectMapper.readValue(
                ResourceUtils.readResource("/import/core_roles_merge/single_model_with_user_roles_and_roles/merged_roles.json"),
                new TypeReference<>() {
                }
        );

        // when
        Map<String, Role> actualResult = coreRolesMerger.mergeCoreRoles(config, true);

        // then
        Assertions.assertThat(actualResult).isEqualTo(expectedResult);
        verifyNoInteractions(roleService);
    }
}