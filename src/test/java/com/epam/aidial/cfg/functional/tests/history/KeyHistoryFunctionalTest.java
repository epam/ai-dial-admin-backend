package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.EntityRevisionDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAdapterDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDtoWithRole;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithAdapter;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static org.mockito.Mockito.doReturn;

public abstract class KeyHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private TestHistoryFacade historyFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;

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

    @Test
    public void shouldCorrectlyTrackRoleUpdatedAtInLatestAndAuditStatesWhenKeyIsCreatedWithRole() {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create key
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        keyFacade.createKey(createKeyDtoWithRole("1"));

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertEquals(Instant.ofEpochMilli(222L), roleFacade.getRole(roleDto.getName()).getUpdatedAt());
        Assertions.assertEquals(Instant.ofEpochMilli(222L), roleFacade.getSnapshot(roleDto.getName(), latestRevision).getUpdatedAt());
    }

    @Test
    public void shouldNotTrackIrrelevantChangesDuringRollback() {
        // create role
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create key
        keyFacade.createKey(createKeyDtoWithRole("1"));

        // create adapter
        adapterFacade.createAdapter(createAdapterDto("1"));

        // create model
        modelFacade.createModel(createModelDtoWithAdapter("1"));

        // remember rev number
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // update model
        ModelDto modelDto = createModelDtoWithAdapter("1");
        modelDto.setDescription("new description");
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        // rollback
        historyFacade.rollbackToRevision(revNumberToRollback);

        // verify
        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        Collection<EntityRevisionDto<RoleDto>> roleEntityRevisions = roleFacade.getEntityRevisionsAt(latestRevision);
        Assertions.assertTrue(roleEntityRevisions.isEmpty());
        Collection<EntityRevisionDto<KeyDto>> keyEntityRevisions = keyFacade.getEntityRevisionsAt(latestRevision);
        Assertions.assertTrue(keyEntityRevisions.isEmpty());
        Collection<EntityRevisionDto<AdapterDto>> adapterEntityRevisions = adapterFacade.getEntityRevisionsAt(latestRevision);
        Assertions.assertTrue(adapterEntityRevisions.isEmpty());

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivities().getData()
                .stream()
                .filter(act -> act.getRevision().equals(latestRevision))
                .toList();
        Assertions.assertTrue(auditActivities.stream().noneMatch(act -> act.getResourceType().equals("Role")));
        Assertions.assertTrue(auditActivities.stream().noneMatch(act -> act.getResourceType().equals("Key")));
        Assertions.assertTrue(auditActivities.stream().noneMatch(act -> act.getResourceType().equals("Adapter")));
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
