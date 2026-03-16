package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAuditActivityDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createKeyDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public abstract class AssociationsManyToManyHistoryFunctionalTests {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private TestHistoryFacade historyFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenKeyIsCreatedWithRoles() {
        // create roles
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto1 = createRoleDto("1");
        roleFacade.createRole(roleDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto2 = createRoleDto("2");
        roleFacade.createRole(roleDto2);

        // create key
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createKeyDto("1");
        keyDto.setRoles(List.of(roleDto1.getName(), roleDto2.getName()));
        keyFacade.createKey(keyDto);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(roleFacade.getRole(roleDto1.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(roleFacade.getRole(roleDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(keyFacade.getKey(keyDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedKeyActivity = createAuditActivityDto("Create", "Key", keyDto.getName(), 333L);
        AuditActivityDto expectedRole1Activity = createAuditActivityDto("Update", "Role", roleDto1.getName(), 333L);
        AuditActivityDto expectedRole2Activity = createAuditActivityDto("Update", "Role", roleDto2.getName(), 333L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedKeyActivity, expectedRole1Activity, expectedRole2Activity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenRoleIsCreatedWithKeys() {
        // create keys
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto1 = createKeyDto("1");
        keyFacade.createKey(keyDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto2 = createKeyDto("2");
        keyFacade.createKey(keyDto2);

        // create role
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleDto.setGrantedKeys(List.of(keyDto1.getName(), keyDto2.getName()));
        roleFacade.createRole(roleDto);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(roleFacade.getRole(roleDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(keyFacade.getKey(keyDto1.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(keyFacade.getKey(keyDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedKey1Activity = createAuditActivityDto("Update", "Key", keyDto1.getName(), 333L);
        AuditActivityDto expectedKey2Activity = createAuditActivityDto("Update", "Key", keyDto2.getName(), 333L);
        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Create", "Role", roleDto.getName(), 333L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedKey1Activity, expectedKey2Activity, expectedRoleActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenKeyIsRemoved() {
        // create keys
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto1 = createKeyDto("1");
        keyFacade.createKey(keyDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto2 = createKeyDto("2");
        keyFacade.createKey(keyDto2);

        // create role
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleDto.setGrantedKeys(List.of(keyDto1.getName(), keyDto2.getName()));
        roleFacade.createRole(roleDto);

        // remove key
        doReturn(444L).when(transactionTimestampContext).getTimestamp();
        keyFacade.deleteKey(keyDto1.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(roleFacade.getRole(roleDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
        Assertions.assertThat(keyFacade.getKey(keyDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedKeyActivity = createAuditActivityDto("Delete", "Key", keyDto1.getName(), 444L);
        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 444L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedKeyActivity, expectedRoleActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenRoleIsRemoved() {
        // create roles
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto1 = createRoleDto("1");
        roleFacade.createRole(roleDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto2 = createRoleDto("2");
        roleFacade.createRole(roleDto2);

        // create key
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createKeyDto("1");
        keyDto.setRoles(List.of(roleDto1.getName(), roleDto2.getName()));
        keyFacade.createKey(keyDto);

        // remove role
        doReturn(444L).when(transactionTimestampContext).getTimestamp();
        roleFacade.deleteRole(roleDto1.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(roleFacade.getRole(roleDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(keyFacade.getKey(keyDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));

        AuditActivityDto expectedKeyActivity = createAuditActivityDto("Update", "Key", keyDto.getName(), 444L);
        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Delete", "Role", roleDto1.getName(), 444L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedKeyActivity, expectedRoleActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelIsCreatedWithInterceptors() {
        // create interceptors
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        // create model
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto = createModelDto("1");
        modelDto.setInterceptors(List.of(interceptorDto1.getName(), interceptorDto2.getName()));
        modelFacade.createModel(modelDto);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(interceptorFacade.getInterceptor(interceptorDto1.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(interceptorFacade.getInterceptor(interceptorDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedModelActivity = createAuditActivityDto("Create", "Model", modelDto.getName(), 333L);
        AuditActivityDto expectedInterceptor1Activity = createAuditActivityDto("Update", "Interceptor", interceptorDto1.getName(), 333L);
        AuditActivityDto expectedInterceptor2Activity = createAuditActivityDto("Update", "Interceptor", interceptorDto2.getName(), 333L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedModelActivity, expectedInterceptor1Activity, expectedInterceptor2Activity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenInterceptorIsCreatedWithModels() {
        // create models
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto1 = createModelDto("1");
        modelFacade.createModel(modelDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto2 = createModelDto("2");
        modelFacade.createModel(modelDto2);

        // create interceptor
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setEntities(List.of(modelDto1.getName(), modelDto2.getName()));
        interceptorFacade.createInterceptor(interceptorDto);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(interceptorFacade.getInterceptor(interceptorDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto1.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedModel1Activity = createAuditActivityDto("Update", "Model", modelDto1.getName(), 333L);
        AuditActivityDto expectedModel2Activity = createAuditActivityDto("Update", "Model", modelDto2.getName(), 333L);
        AuditActivityDto expectedInterceptorActivity = createAuditActivityDto("Create", "Interceptor", interceptorDto.getName(), 333L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedModel1Activity, expectedModel2Activity, expectedInterceptorActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelIsRemoved() {
        // create models
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto1 = createModelDto("1");
        modelFacade.createModel(modelDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto2 = createModelDto("2");
        modelFacade.createModel(modelDto2);

        // create interceptor
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setEntities(List.of(modelDto1.getName(), modelDto2.getName()));
        interceptorFacade.createInterceptor(interceptorDto);

        // remove model
        doReturn(444L).when(transactionTimestampContext).getTimestamp();
        modelFacade.deleteModel(modelDto1.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(interceptorFacade.getInterceptor(interceptorDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
        Assertions.assertThat(modelFacade.getModel(modelDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedModelActivity = createAuditActivityDto("Delete", "Model", modelDto1.getName(), 444L);
        AuditActivityDto expectedInterceptorActivity = createAuditActivityDto("Update", "Interceptor", interceptorDto.getName(), 444L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedModelActivity, expectedInterceptorActivity);
    }

    //    todo: uncomment
    //
    //    @Test
    //    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenFirstInterceptorIsRemoved() {
    //        // create interceptors
    //        doReturn(111L).when(transactionTimestampContext).getTimestamp();
    //        InterceptorDto interceptorDto1 = createInterceptorDto("1");
    //        interceptorFacade.createInterceptor(interceptorDto1);
    //
    //        doReturn(222L).when(transactionTimestampContext).getTimestamp();
    //        InterceptorDto interceptorDto2 = createInterceptorDto("2");
    //        interceptorFacade.createInterceptor(interceptorDto2);
    //
    //        // create model
    //        doReturn(333L).when(transactionTimestampContext).getTimestamp();
    //        ModelDto modelDto = createModelDto("1");
    //        modelDto.setInterceptors(List.of(interceptorDto1.getName(), interceptorDto2.getName()));
    //        modelFacade.createModel(modelDto);
    //
    //        // remove interceptor
    //        doReturn(444L).when(transactionTimestampContext).getTimestamp();
    //        interceptorFacade.deleteInterceptor(interceptorDto1.getName());
    //
    //        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
    //
    //        // verify
    //        Assertions.assertThat(interceptorFacade.getInterceptor(interceptorDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
    //        Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
    //
    //        AuditActivityDto expectedModelActivity = createAuditActivityDto("Update", "Model", modelDto.getName(), 444L);
    //        AuditActivityDto expectedInterceptor1Activity = createAuditActivityDto("Delete", "Interceptor", interceptorDto1.getName(), 444L);
    //        // interceptor2 is considered as updated due to changed order.
    //        // before interceptor1 deletion order was: 0 - interceptor1, 1 - interceptor2
    //        // after interceptor1 deletion order becomes: 0 - interceptor2
    //        AuditActivityDto expectedInterceptor2Activity = createAuditActivityDto("Update", "Interceptor", interceptorDto2.getName(), 444L);
    //
    //        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
    //        assertThat(auditActivities)
    //                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
    //                .containsExactlyInAnyOrder(expectedModelActivity, expectedInterceptor1Activity, expectedInterceptor2Activity);
    //    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenSecondInterceptorIsRemoved() {
        // create interceptors
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        // create model
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto = createModelDto("1");
        modelDto.setInterceptors(List.of(interceptorDto1.getName(), interceptorDto2.getName()));
        modelFacade.createModel(modelDto);

        // remove interceptor
        doReturn(444L).when(transactionTimestampContext).getTimestamp();
        interceptorFacade.deleteInterceptor(interceptorDto2.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(interceptorFacade.getInterceptor(interceptorDto1.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));

        AuditActivityDto expectedModelActivity = createAuditActivityDto("Update", "Model", modelDto.getName(), 444L);
        AuditActivityDto expectedInterceptorActivity = createAuditActivityDto("Delete", "Interceptor", interceptorDto2.getName(), 444L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedModelActivity, expectedInterceptorActivity);
    }
}
