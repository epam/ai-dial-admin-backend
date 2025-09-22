package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class ToolSetHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private ToolSetFacade toolSetFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    private void initRoles() {
        RoleDto role1 = new RoleDto();
        role1.setName("role1");
        role1.setDescription("role1");

        RoleDto role2 = new RoleDto();
        role2.setName("role2");
        role2.setDescription("role2");

        RoleDto role3 = new RoleDto();
        role3.setName("role3");
        role3.setDescription("role3");

        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
        roleFacade.createRole(role3);
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateToolSet() {
        initRoles();

        // 1. Create ToolSet1
        ToolSetDto toolSetDto = createDto("1");
        toolSetDto.setEndpoint("endpoint1");
        toolSetFacade.createToolSet(toolSetDto);

        // 2. Update ToolSet1 description
        ToolSetDto updatedToolSet = createDto("1");
        updatedToolSet.setDescription("New ToolSet description");
        updatedToolSet.setEndpoint("endpoint2");
        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "*");

        // 3. Verify ToolSet1
        ToolSetDto actual = toolSetFacade.getToolSet(toolSetDto.getName());
        var expected = createDto("1");
        expected.setDescription("New ToolSet description");
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        expected.setEndpoint("endpoint2");
        assertToolSet(actual, expected);

        // 4. Add roles to ToolSet1
        updatedToolSet.setDefaultRoleLimit(new LimitDto());
        updatedToolSet.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        updatedToolSet.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        updatedToolSet.setRoleShareResourceLimits(Map.of("role2", new ShareResourceLimitDto(), "role3", new ShareResourceLimitDto()));
        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "*");
        actual = toolSetFacade.getToolSet(toolSetDto.getName());
        assertToolSet(actual, updatedToolSet);

        // 5. Update ToolSet1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(20L);
        updatedToolSet.setRoleLimits(Map.of("role3", limitDto));
        updatedToolSet.setRoleShareResourceLimits(Map.of("role3", shareResourceLimitDto));
        toolSetFacade.updateToolSet(toolSetDto.getName(), updatedToolSet, "*");
        var actualAtOldRevision = toolSetFacade.getAllToolSets();
        actual = toolSetFacade.getToolSet(toolSetDto.getName());
        assertToolSet(actual, updatedToolSet);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // 6. Delete role3
        roleFacade.deleteRole("role3");
        actual = toolSetFacade.getToolSet(toolSetDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());
        Assertions.assertTrue(actual.getRoleShareResourceLimits().isEmpty());

        // 7. Delete ToolSet1
        toolSetFacade.deleteToolSet(toolSetDto.getName());

        // 7. Create ToolSet2
        ToolSetDto toolSetDto2 = createDto("2");
        toolSetDto2.setEndpoint("endpoint3");
        toolSetFacade.createToolSet(toolSetDto2);

        // 9. Create role3
        RoleDto role3 = new RoleDto();
        role3.setName("role3");
        role3.setDescription("role3");
        roleFacade.createRole(role3);

        // 10. Create ToolSet3 with assigned role3
        ToolSetDto toolSetDto3 = createDto("3");
        toolSetDto3.setEndpoint("endpoint4");
        toolSetFacade.createToolSet(toolSetDto3);

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ToolSetDto> toolSetsAfterRollback = toolSetFacade.getAllToolSets();
        Assertions.assertEquals(actualAtOldRevision, toolSetsAfterRollback);
    }

    private void assertToolSet(ToolSetDto actual, ToolSetDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private ToolSetDto createDto(String suffix) {
        ToolSetDto toolSet = new ToolSetDto();
        toolSet.setName("ToolSet" + suffix);
        toolSet.setDescription("description" + suffix);
        toolSet.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        toolSet.setRoleShareResourceLimits(Map.of(
                "role" + suffix, new ShareResourceLimitDto()
        ));
        toolSet.setMaxRetryAttempts(1);
        return toolSet;
    }
}
