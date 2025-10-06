package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.web.facade.AssistantFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AssistantHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private AssistantFacade assistantFacade;
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
    public void shouldSuccessfullyCreateAndUpdateAssistant() {
        initRoles();

        // 1 create assistant1
        AssistantDto assistantDto = createDto("1");
        assistantFacade.createAssistant(assistantDto);

        // 2 update assistant1 description
        AssistantDto updatedAssistant = createDto("1");
        updatedAssistant.setDescription("new assistant description");
        assistantFacade.updateAssistant(assistantDto.getName(), updatedAssistant);

        // verify assistant1
        AssistantDto actual = assistantFacade.getAssistant(assistantDto.getName());
        var expected = createDto("1");
        expected.setDescription("new assistant description");
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setDefaults(Map.of());
        assertAssistant(actual, expected);

        // 3 add roles to assistant1
        updatedAssistant.setDefaultRoleLimit(new LimitDto());
        updatedAssistant.setDefaults(Map.of());
        updatedAssistant.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        assistantFacade.updateAssistant(assistantDto.getName(), updatedAssistant);
        actual = assistantFacade.getAssistant(assistantDto.getName());
        assertAssistant(actual, updatedAssistant);

        // 4 update assistant1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(20L);
        updatedAssistant.setRoleLimits(Map.of("role3", limitDto));
        assistantFacade.updateAssistant(assistantDto.getName(), updatedAssistant);
        var actualAtRevision7 = assistantFacade.getAssistant(assistantDto.getName());
        assertAssistant(actualAtRevision7, updatedAssistant);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // 5 delete role3
        roleFacade.deleteRole("role3");
        actual = assistantFacade.getAssistant(assistantDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());

        // 6 delete assistant 1
        assistantFacade.deleteAssistant(assistantDto.getName());

        // 7 create assistant 2
        assistantFacade.createAssistant(createDto("2"));

        // 8 create role3
        RoleDto role3 = new RoleDto();
        role3.setName("role3");
        role3.setDescription("role3");
        roleFacade.createRole(role3);

        // 9 create assistant3 with assigned role3
        assistantFacade.createAssistant(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<AssistantDto> assistantsAfterRollbackToRevision7 = assistantFacade.getAllAssistants();
        Assertions.assertEquals(List.of(actualAtRevision7), assistantsAfterRollbackToRevision7);
    }

    private void assertAssistant(AssistantDto actual, AssistantDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private AssistantDto createDto(String suffix) {
        AssistantDto assistantDto = new AssistantDto();
        assistantDto.setName("assistant" + suffix);
        assistantDto.setDescription("description" + suffix);
        assistantDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return assistantDto;
    }
}
