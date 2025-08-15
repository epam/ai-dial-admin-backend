package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class RolesHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldSuccessfullyCreateAndUpdateRole() {

        ModelDto model1 = createModelDto("1");
        ModelDto model2 = createModelDto("2");
        modelFacade.createModel(model1);
        modelFacade.createModel(model2);

        KeyDto key1 = createKeyDto("1");
        KeyDto key2 = createKeyDto("2");
        KeyDto key3 = createKeyDto("3");
        keyFacade.createKey(key1);
        keyFacade.createKey(key2);
        keyFacade.createKey(key3);

        // create role1
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);

        // update role1 description
        RoleDto updatedRole = createDto("1");
        updatedRole.setDescription("new role description");
        LimitDto limit1 = new LimitDto();
        limit1.setMinute(10L);
        LimitDto limit2 = new LimitDto();
        limit2.setMinute(20L);
        ShareResourceLimitDto shareResourceLimit1 = new ShareResourceLimitDto();
        shareResourceLimit1.setMaxAcceptedUsers(10);
        ShareResourceLimitDto shareResourceLimit2 = new ShareResourceLimitDto();
        shareResourceLimit2.setInvitationTtl(20L);
        updatedRole.setLimits(Map.of("model1", limit1, "model2", limit2));
        updatedRole.setShare(Map.of("model1", shareResourceLimit1, "model2", shareResourceLimit2));
        updatedRole.setGrantedKeys(List.of("key1", "key2"));
        roleFacade.updateRole(roleDto.getName(), updatedRole);

        // verify role1
        RoleDto actual = roleFacade.getRole(roleDto.getName());
        assertRole(actual, updatedRole);

        var actualRoleAtRevision = roleFacade.getRole(roleDto.getName());
        var actualRolesAtRevision = roleFacade.getAllRoles();
        assertRole(actualRoleAtRevision, updatedRole);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        modelFacade.deleteModel(model1.getName());
        modelFacade.deleteModel(model2.getName());

        updatedRole = roleFacade.getRole(roleDto.getName());
        updatedRole.setDescription("new new role description");
        updatedRole.setGrantedKeys(List.of("key2", "key3"));
        roleFacade.updateRole(roleDto.getName(), updatedRole);

        keyFacade.deleteKey("key1");
        // delete role 1
        roleFacade.deleteRole(roleDto.getName());

        // create role 2
        roleFacade.createRole(createDto("2"));

        // create role3
        roleFacade.createRole(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<RoleDto> rolesAfterRollbackToRevision = roleFacade.getAllRoles();
        var actualRole = roleFacade.getRole(actualRoleAtRevision.getName());

        actualRolesAtRevision.forEach(role -> Collections.sort(role.getGrantedKeys()));
        rolesAfterRollbackToRevision.forEach(role -> Collections.sort(role.getGrantedKeys()));
        Assertions.assertEquals(actualRolesAtRevision, rolesAfterRollbackToRevision);

        Collections.sort(actualRoleAtRevision.getGrantedKeys());
        Collections.sort(actualRole.getGrantedKeys());
        Assertions.assertEquals(actualRoleAtRevision, actualRole);
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateRoleWithoutDeletion() {

        ModelDto model1 = createModelDto("1");
        ModelDto model2 = createModelDto("2");
        modelFacade.createModel(model1);
        modelFacade.createModel(model2);

        KeyDto key1 = createKeyDto("1");
        KeyDto key2 = createKeyDto("2");
        KeyDto key3 = createKeyDto("3");
        keyFacade.createKey(key1);
        keyFacade.createKey(key2);
        keyFacade.createKey(key3);

        // create role1
        RoleDto roleDto = createDto("1");
        roleFacade.createRole(roleDto);

        // update role1 description
        RoleDto updatedRole = createDto("1");
        updatedRole.setDescription("new role description");
        LimitDto limit1 = new LimitDto();
        limit1.setMinute(10L);
        LimitDto limit2 = new LimitDto();
        limit2.setMinute(20L);
        ShareResourceLimitDto shareResourceLimit1 = new ShareResourceLimitDto();
        shareResourceLimit1.setMaxAcceptedUsers(10);
        ShareResourceLimitDto shareResourceLimit2 = new ShareResourceLimitDto();
        shareResourceLimit2.setInvitationTtl(20L);
        updatedRole.setLimits(Map.of("model1", limit1, "model2", limit2));
        updatedRole.setShare(Map.of("model1", shareResourceLimit1, "model2", shareResourceLimit2));
        updatedRole.setGrantedKeys(List.of("key1", "key2"));
        roleFacade.updateRole(roleDto.getName(), updatedRole);

        // verify role1
        RoleDto actual = roleFacade.getRole(roleDto.getName());
        assertRole(actual, updatedRole);

        var actualRoleAtRevision = roleFacade.getRole(roleDto.getName());
        var actualRolesAtRevision = roleFacade.getAllRoles();
        assertRole(actualRoleAtRevision, updatedRole);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        limit1.setMinute(100L);
        limit2.setMinute(200L);
        shareResourceLimit1.setMaxAcceptedUsers(100);
        shareResourceLimit2.setInvitationTtl(200L);
        updatedRole.setLimits(Map.of("model1", limit1, "model2", limit2));
        updatedRole.setShare(Map.of("model1", shareResourceLimit1, "model2", shareResourceLimit2));
        updatedRole.setDescription("new new role description");
        updatedRole.setGrantedKeys(List.of("key2", "key3"));
        roleFacade.updateRole(roleDto.getName(), updatedRole);

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<RoleDto> rolesAfterRollbackToRevision = roleFacade.getAllRoles();
        var actualRole = roleFacade.getRole(actualRoleAtRevision.getName());
        actualRolesAtRevision.stream().map(RoleDto::getGrantedKeys).forEach(Collections::sort);
        rolesAfterRollbackToRevision.stream().map(RoleDto::getGrantedKeys).forEach(Collections::sort);
        Assertions.assertEquals(actualRolesAtRevision, rolesAfterRollbackToRevision);
        Collections.sort(actualRoleAtRevision.getGrantedKeys());
        Collections.sort(actualRole.getGrantedKeys());
        Assertions.assertEquals(actualRoleAtRevision, actualRole);
    }

    private ModelDto createModelDto(String suffix) {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model" + suffix);
        modelDto.setDescription("description" + suffix);
        return modelDto;
    }

    private KeyDto createKeyDto(String suffix) {
        KeyDto keyDto = new KeyDto();
        keyDto.setName("key" + suffix);
        keyDto.setKey("key value" + suffix);
        keyDto.setDescription("description" + suffix);
        return keyDto;
    }

    private void assertRole(RoleDto actual, RoleDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private RoleDto createDto(String suffix) {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("role" + suffix);
        roleDto.setDescription("description" + suffix);
        roleDto.setLimits(Map.of());
        roleDto.setShare(Map.of());
        roleDto.setGrantedKeys(List.of());
        return roleDto;
    }
}
