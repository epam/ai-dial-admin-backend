package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.web.facade.AddonFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AddonHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private AddonFacade addonFacade;
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
    public void shouldSuccessfullyCreateAndUpdateAddon() {
        initRoles();

        // 1 create addon1
        AddonDto addonDto = createDto("1");
        addonFacade.createAddon(addonDto);

        // 2 update addon1 description
        AddonDto updatedAddon = createDto("1");
        updatedAddon.setDescription("new addon description");
        addonFacade.updateAddon(addonDto.getName(), updatedAddon);

        // verify addon1
        AddonDto actual = addonFacade.getAddon(addonDto.getName());
        var expected = createDto("1");
        expected.setDescription("new addon description");
        expected.setDefaultRoleLimit(new LimitDto());
        assertAddon(actual, expected);

        // 3 add roles to addon1
        updatedAddon.setDefaultRoleLimit(new LimitDto());
        updatedAddon.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        addonFacade.updateAddon(addonDto.getName(), updatedAddon);
        actual = addonFacade.getAddon(addonDto.getName());
        assertAddon(actual, updatedAddon);

        // 4 update addon1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        updatedAddon.setRoleLimits(Map.of("role3", limitDto));
        addonFacade.updateAddon(addonDto.getName(), updatedAddon);
        var actualAtRevision = addonFacade.getAddon(addonDto.getName());
        assertAddon(actualAtRevision, updatedAddon);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // 5 delete role3
        roleFacade.deleteRole("role3");
        actual = addonFacade.getAddon(addonDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());

        // 6 delete addon 1
        addonFacade.deleteAddon(addonDto.getName());

        // 7 create addon 2
        addonFacade.createAddon(createDto("2"));

        // 8 create role3
        RoleDto role3 = new RoleDto();
        role3.setName("role3");
        role3.setDescription("role3");
        roleFacade.createRole(role3);

        // 9 create addon3 with assigned role3
        addonFacade.createAddon(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<AddonDto> addonsAfterRollbackToOldRevision = addonFacade.getAllAddons();
        Assertions.assertEquals(List.of(actualAtRevision), addonsAfterRollbackToOldRevision);
    }

    private void assertAddon(AddonDto actual, AddonDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private AddonDto createDto(String suffix) {
        AddonDto addonDto = new AddonDto();
        addonDto.setName("addon" + suffix);
        addonDto.setDescription("description" + suffix);
        addonDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return addonDto;
    }
}
