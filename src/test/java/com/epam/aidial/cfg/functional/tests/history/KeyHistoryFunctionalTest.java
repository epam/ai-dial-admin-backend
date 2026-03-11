package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDtoWithRole;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;

public abstract class KeyHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
        roleFacade.createRole(createRoleDto("3"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateKey() {
        initRoles();

        // create key1
        KeyDto keyDto = createKeyDtoWithRole("1");
        keyFacade.createKey(keyDto);

        // update key1 description
        KeyDto updatedKey = createKeyDtoWithRole("1");
        updatedKey.setDescription("new key description");
        keyFacade.updateKey(keyDto.getName(), updatedKey, "*");

        // verify key1
        KeyDto actual = keyFacade.getKey(keyDto.getName());
        var expected = createKeyDtoWithRole("1");
        expected.setDescription("new key description");
        assertKey(actual, expected);

        // add roles to key1
        updatedKey.setRoles(List.of("role1", "role2", "role3"));
        keyFacade.updateKey(keyDto.getName(), updatedKey, "*");
        actual = keyFacade.getKey(keyDto.getName());
        assertKey(actual, updatedKey);

        var actualAtRevision = keyFacade.getKey(keyDto.getName());
        assertKey(actualAtRevision, updatedKey);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // delete role3
        roleFacade.deleteRole("role3");
        keyFacade.getKey(keyDto.getName());

        // delete key 1
        keyFacade.deleteKey(keyDto.getName());

        // create key 2
        keyFacade.createKey(createKeyDtoWithRole("2"));

        // create role3
        RoleDto role3 = createRoleDto("3");
        roleFacade.createRole(role3);

        // create key3 with assigned role3
        keyFacade.createKey(createKeyDtoWithRole("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<KeyDto> keysAfterRollbackToRevision = keyFacade.getAllKeys();
        assertKeysWithoutKey(List.of(actualAtRevision), keysAfterRollbackToRevision.stream().toList());
    }

    private void assertKeysWithoutKey(List<KeyDto> actual, List<KeyDto> expected) {
        Assertions.assertEquals(actual.size(), expected.size());
        for (int i = 0; i < actual.size(); i++) {
            KeyDto actualKey = actual.get(i);
            KeyDto expectedKey = expected.get(i);
            assertKeyWithoutKey(actualKey, expectedKey);
        }
    }

    private void assertKey(KeyDto actual, KeyDto expected) {
        Assertions.assertEquals(expected.getKey(), actual.getKey());
        assertKeyWithoutKey(actual, expected);
    }

    private static void assertKeyWithoutKey(KeyDto actual, KeyDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getProject(), actual.getProject());
        Assertions.assertEquals(expected.isSecured(), actual.isSecured());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        org.assertj.core.api.Assertions.assertThat(actual.getRoles())
                .containsExactlyInAnyOrderElementsOf(expected.getRoles());
    }
}
