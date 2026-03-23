package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAdapterDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAuditActivityDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createBaseApplicationDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithAdapter;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public abstract class AssociationsOneToManyHistoryFunctionalTests {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Autowired
    private AdapterFacade adapterFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private ApplicationTypeSchemaFacade applicationTypeSchemaFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private TestHistoryFacade historyFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelIsCreatedWithAdapter() {
        // create adapter
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        AdapterDto adapterDto = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto);

        // create model
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto = createModelDtoWithAdapter("1");
        modelFacade.createModel(modelDto);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(adapterFacade.getAdapter(adapterDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(222L));
        Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(222L));

        AuditActivityDto expectedAdapterActivity = createAuditActivityDto("Update", "Adapter", adapterDto.getName(), 222L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Create", "Model", modelDto.getName(), 222L);

        assertAuditActivities(latestRevision, expectedAdapterActivity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenAdapterIsCreatedWithModels() {
        // create models
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto1 = createModelDto("1");
        modelFacade.createModel(modelDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto2 = createModelDto("2");
        modelFacade.createModel(modelDto2);

        // create adapter
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        AdapterDto adapterDto = createAdapterDto("1");
        adapterDto.setModels(List.of(modelDto1.getName(), modelDto2.getName()));
        adapterFacade.createAdapter(adapterDto);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(adapterFacade.getAdapter(adapterDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto1.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedAdapterActivity = createAuditActivityDto("Create", "Adapter", adapterDto.getName(), 333L);
        AuditActivityDto expectedModel1Activity = createAuditActivityDto("Update", "Model", modelDto1.getName(), 333L);
        AuditActivityDto expectedModel2Activity = createAuditActivityDto("Update", "Model", modelDto2.getName(), 333L);

        assertAuditActivities(latestRevision, expectedAdapterActivity, expectedModel1Activity, expectedModel2Activity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelWithAdapterIsRemoved() {
        // create adapter
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        AdapterDto adapterDto = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto);

        // create model
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto = createModelDtoWithAdapter("1");
        modelFacade.createModel(modelDto);

        // remove model
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        modelFacade.deleteModel(modelDto.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(adapterFacade.getAdapter(adapterDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedAdapterActivity = createAuditActivityDto("Update", "Adapter", adapterDto.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Delete", "Model", modelDto.getName(), 333L);

        assertAuditActivities(latestRevision, expectedAdapterActivity, expectedModelActivity);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenAdapterIsRemoved(boolean removeModel) {
        // create adapter
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        AdapterDto adapterDto = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto);

        // create model
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto = createModelDtoWithAdapter("1");
        modelFacade.createModel(modelDto);

        // remove adapter
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        adapterFacade.deleteAdapter(adapterDto.getName(), removeModel);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        if (!removeModel) {
            Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        }

        String expectedModelActivityType = removeModel ? "Delete" : "Update";
        AuditActivityDto expectedAdapterActivity = createAuditActivityDto("Delete", "Adapter", adapterDto.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto(expectedModelActivityType, "Model", modelDto.getName(), 333L);

        assertAuditActivities(latestRevision, expectedAdapterActivity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenRoleIsCreatedWithModelLimits() {
        // create models
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto1 = createModelDto("1");
        modelFacade.createModel(modelDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto2 = createModelDto("2");
        modelFacade.createModel(modelDto2);

        // create role with model limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        RoleDto roleDto = createRoleDto("1");
        roleDto.setLimits(Map.of(modelDto1.getName(), limitDto, modelDto2.getName(), limitDto));
        roleFacade.createRole(roleDto);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(roleFacade.getRole(roleDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto1.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Create", "Role", roleDto.getName(), 333L);
        AuditActivityDto expectedModel1Activity = createAuditActivityDto("Update", "Model", modelDto1.getName(), 333L);
        AuditActivityDto expectedModel2Activity = createAuditActivityDto("Update", "Model", modelDto2.getName(), 333L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedModel1Activity, expectedModel2Activity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelIsCreatedWithRoleLimits() {
        // create roles
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto1 = createRoleDto("1");
        roleFacade.createRole(roleDto1);

        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto2 = createRoleDto("2");
        roleFacade.createRole(roleDto2);

        // create model with role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto modelDto = createModelDto("1");
        modelDto.setRoleLimits(Map.of(roleDto1.getName(), limitDto, roleDto2.getName(), limitDto));
        modelFacade.createModel(modelDto);

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(roleFacade.getRole(roleDto1.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(roleFacade.getRole(roleDto2.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedRole1Activity = createAuditActivityDto("Update", "Role", roleDto1.getName(), 333L);
        AuditActivityDto expectedRole2Activity = createAuditActivityDto("Update", "Role", roleDto2.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Create", "Model", modelDto.getName(), 333L);

        assertAuditActivities(latestRevision, expectedRole1Activity, expectedRole2Activity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenRoleLimitIsUpdatedViaModel() {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model with role limit
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto modelDto = createModelDto("1");
        modelDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.createModel(modelDto);

        // update role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        LimitDto newLimitDto = new LimitDto();
        newLimitDto.setDay(20L);

        modelDto = createModelDto("1");
        modelDto.setRoleLimits(Map.of(roleDto.getName(), newLimitDto));
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(roleFacade.getRole(roleDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Update", "Model", modelDto.getName(), 333L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenRoleLimitIsUpdatedViaRole() {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model with role limit
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto modelDto = createModelDto("1");
        modelDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.createModel(modelDto);

        // update role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        LimitDto newLimitDto = new LimitDto();
        newLimitDto.setDay(20L);

        roleDto = createRoleDto("1");
        roleDto.setLimits(Map.of(modelDto.getName(), newLimitDto));
        roleFacade.updateRole(roleDto.getName(), roleDto, "*");

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(roleFacade.getRole(roleDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Update", "Model", modelDto.getName(), 333L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelRoleLimitIsRemovedViaModel() {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model with role limit
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto modelDto = createModelDto("1");
        modelDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.createModel(modelDto);

        // remove role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        modelDto = createModelDto("1");
        modelDto.setRoleLimits(null);
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        RoleDto role = roleFacade.getRole(roleDto.getName());
        Assertions.assertThat(role.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(role.getLimits()).isEmpty();
        ModelDto model = modelFacade.getModel(modelDto.getName());
        Assertions.assertThat(model.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(model.getRoleLimits()).isEmpty();

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Update", "Model", modelDto.getName(), 333L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelRoleLimitIsRemovedViaRole() {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model with role limit
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto modelDto = createModelDto("1");
        modelDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.createModel(modelDto);

        // remove role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        roleDto = createRoleDto("1");
        roleDto.setLimits(null);
        roleFacade.updateRole(roleDto.getName(), roleDto, "*");

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        RoleDto role = roleFacade.getRole(roleDto.getName());
        Assertions.assertThat(role.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(role.getLimits()).isEmpty();
        ModelDto model = modelFacade.getModel(modelDto.getName());
        Assertions.assertThat(model.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(model.getRoleLimits()).isEmpty();

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Update", "Model", modelDto.getName(), 333L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenRoleWithModelRoleLimitIsRemoved() {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model with role limit
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto modelDto = createModelDto("1");
        modelDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.createModel(modelDto);

        // remove role
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        roleFacade.deleteRole(roleDto.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThatThrownBy(() -> roleFacade.getRole(roleDto.getName())).isInstanceOf(EntityNotFoundException.class);
        ModelDto model = modelFacade.getModel(modelDto.getName());
        Assertions.assertThat(model.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(model.getRoleLimits()).isEmpty();

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Delete", "Role", roleDto.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Update", "Model", modelDto.getName(), 333L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertAuditActivities(latestRevision, expectedRoleActivity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelWithRoleLimitIsRemoved() {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model with role limit
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto modelDto = createModelDto("1");
        modelDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.createModel(modelDto);

        // remove model
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        modelFacade.deleteModel(modelDto.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        RoleDto role = roleFacade.getRole(roleDto.getName());
        Assertions.assertThat(role.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(333L));
        Assertions.assertThat(role.getLimits()).isEmpty();
        Assertions.assertThatThrownBy(() -> modelFacade.getModel(modelDto.getName())).isInstanceOf(EntityNotFoundException.class);

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 333L);
        AuditActivityDto expectedModelActivity = createAuditActivityDto("Delete", "Model", modelDto.getName(), 333L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenAppRoleLimitIsRemovedViaAppWithSchema() throws Exception {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create app schema
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        String appSchemaJson = ResourceUtils.readResource("/application_type_schema_dto.json");
        ApplicationTypeSchemaDto appSchemaDto = OBJECT_MAPPER.readValue(appSchemaJson, new TypeReference<>() {
        });
        applicationTypeSchemaFacade.create(appSchemaDto);

        // create app with schema and with role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(new URI(appSchemaDto.getId()));
        applicationDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        applicationFacade.createApplication(applicationDto);

        // remove role limit
        doReturn(444L).when(transactionTimestampContext).getTimestamp();
        applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(new URI(appSchemaDto.getId()));
        applicationDto.setRoleLimits(null);
        applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "*");

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        RoleDto role = roleFacade.getRole(roleDto.getName());
        Assertions.assertThat(role.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
        Assertions.assertThat(role.getLimits()).isEmpty();
        ApplicationDto application = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertThat(application.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
        Assertions.assertThat(application.getRoleLimits()).isEmpty();

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 444L);
        AuditActivityDto expectedApplicationActivity = createAuditActivityDto("Update", "Application", applicationDto.getName(), 444L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedApplicationActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenAppRoleLimitIsRemovedViaRole() throws Exception {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create app schema
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        String appSchemaJson = ResourceUtils.readResource("/application_type_schema_dto.json");
        ApplicationTypeSchemaDto appSchemaDto = OBJECT_MAPPER.readValue(appSchemaJson, new TypeReference<>() {
        });
        applicationTypeSchemaFacade.create(appSchemaDto);

        // create app with schema and with role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(new URI(appSchemaDto.getId()));
        applicationDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        applicationFacade.createApplication(applicationDto);

        // remove role limit
        doReturn(444L).when(transactionTimestampContext).getTimestamp();
        roleDto = createRoleDto("1");
        roleDto.setLimits(null);
        roleFacade.updateRole(roleDto.getName(), roleDto, "*");

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        RoleDto role = roleFacade.getRole(roleDto.getName());
        Assertions.assertThat(role.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
        Assertions.assertThat(role.getLimits()).isEmpty();
        ApplicationDto application = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertThat(application.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
        Assertions.assertThat(application.getRoleLimits()).isEmpty();

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 444L);
        AuditActivityDto expectedApplicationActivity = createAuditActivityDto("Update", "Application", applicationDto.getName(), 444L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedApplicationActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenRoleWithAppRoleLimitIsRemoved() throws Exception {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create app schema
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        String appSchemaJson = ResourceUtils.readResource("/application_type_schema_dto.json");
        ApplicationTypeSchemaDto appSchemaDto = OBJECT_MAPPER.readValue(appSchemaJson, new TypeReference<>() {
        });
        applicationTypeSchemaFacade.create(appSchemaDto);

        // create app with schema and with role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(new URI(appSchemaDto.getId()));
        applicationDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        applicationFacade.createApplication(applicationDto);

        // remove role
        doReturn(444L).when(transactionTimestampContext).getTimestamp();
        roleFacade.deleteRole(roleDto.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThatThrownBy(() -> roleFacade.getRole(roleDto.getName())).isInstanceOf(EntityNotFoundException.class);
        ApplicationDto application = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertThat(application.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
        Assertions.assertThat(application.getRoleLimits()).isEmpty();

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Delete", "Role", roleDto.getName(), 444L);
        AuditActivityDto expectedApplicationActivity = createAuditActivityDto("Update", "Application", applicationDto.getName(), 444L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedApplicationActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenAppWithRoleLimitIsRemoved() throws Exception {
        // create role
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create app schema
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        String appSchemaJson = ResourceUtils.readResource("/application_type_schema_dto.json");
        ApplicationTypeSchemaDto appSchemaDto = OBJECT_MAPPER.readValue(appSchemaJson, new TypeReference<>() {
        });
        applicationTypeSchemaFacade.create(appSchemaDto);

        // create app with schema and with role limit
        doReturn(333L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(new URI(appSchemaDto.getId()));
        applicationDto.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        applicationFacade.createApplication(applicationDto);

        // remove model
        doReturn(444L).when(transactionTimestampContext).getTimestamp();
        applicationFacade.deleteApplication(applicationDto.getName());

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        RoleDto role = roleFacade.getRole(roleDto.getName());
        Assertions.assertThat(role.getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(444L));
        Assertions.assertThat(role.getLimits()).isEmpty();
        Assertions.assertThatThrownBy(() -> applicationFacade.getApplication(applicationDto.getName())).isInstanceOf(EntityNotFoundException.class);

        AuditActivityDto expectedRoleActivity = createAuditActivityDto("Update", "Role", roleDto.getName(), 444L);
        AuditActivityDto expectedApplicationActivity = createAuditActivityDto("Delete", "Application", applicationDto.getName(), 444L);
        AuditActivityDto expectedAppSchemaActivity = createAuditActivityDto("Update", "ApplicationTypeSchema", appSchemaDto.getId(), 444L);

        assertAuditActivities(latestRevision, expectedRoleActivity, expectedApplicationActivity, expectedAppSchemaActivity);
    }

    private void assertAuditActivities(Integer revision, AuditActivityDto... expectedActivities) {
        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(revision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedActivities);
    }
}
