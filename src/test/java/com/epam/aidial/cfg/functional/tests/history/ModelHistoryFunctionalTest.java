package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.source.ModelAdapterSourceDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.DeploymentFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAdapterDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithAdapter;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDtoWithLimitsAndEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;

public abstract class ModelHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private DeploymentFacade deploymentFacade;
    @Autowired
    private TestHistoryFacade historyFacade;
    @Autowired
    private AdapterFacade adapterFacade;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
        roleFacade.createRole(createRoleDto("3"));
    }

    @Test
    public void shouldSuccessfullyRollbackModels() {
        initRoles();

        // create model1
        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelFacade.createModel(modelDto);

        // update model1 description
        ModelDto updatedModel = createModelDtoWithLimitsAndEndpoint("1");
        updatedModel.setDescription("new model description");
        updatedModel.setDefaults(Map.of());
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");

        // verify model1
        ModelDto actual = modelFacade.getModel(modelDto.getName());
        var expected = createModelDtoWithLimitsAndEndpoint("1");
        expected.setDescription("new model description");
        expected.setDefaults(Map.of());
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setMaxRetryAttempts(1);
        assertModel(actual, expected);

        // add roles to model1
        updatedModel.setMaxRetryAttempts(3);
        updatedModel.setDefaults(Map.of());
        updatedModel.setDefaultRoleLimit(new LimitDto());
        updatedModel.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");
        actual = modelFacade.getModel(modelDto.getName());
        assertModel(actual, updatedModel);

        // update model1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(20L);
        updatedModel.setRoleLimits(Map.of("role3", limitDto));
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");
        var actualAtRevision = modelFacade.getModel(modelDto.getName());
        assertModel(actualAtRevision, updatedModel);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // delete role3
        roleFacade.deleteRole("role3");
        actual = modelFacade.getModel(modelDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());

        // delete model 1
        modelFacade.deleteModel(modelDto.getName());

        // create model 2
        modelFacade.createModel(createModelDtoWithLimitsAndEndpoint("2"));

        // create role3
        RoleDto role3 = createRoleDto("3");
        roleFacade.createRole(role3);

        // create model3 with assigned role3
        modelFacade.createModel(createModelDtoWithLimitsAndEndpoint("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(List.of(actualAtRevision), modelsAfterRollbackToRevision);

        Assertions.assertThrows(EntityNotFoundException.class, () -> deploymentFacade.ensureExists("model2"));
        Assertions.assertThrows(EntityNotFoundException.class, () -> deploymentFacade.ensureExists("model3"));
    }

    @Test
    public void shouldSuccessfullyRollbackModelsWithInterceptors() {
        initRoles();

        // create interceptor1
        InterceptorDto interceptor1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptor1);
        // create model1
        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setInterceptors(List.of(interceptor1.getName()));
        modelFacade.createModel(modelDto);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = modelFacade.getAll();

        // create interceptor1
        InterceptorDto interceptor2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptor2);

        // update model
        modelDto.setInterceptors(List.of(interceptor2.getName()));
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(actualAtRevision, modelsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackModelsWithInterceptorsWhenInterceptorDeleted() {
        initRoles();

        // create interceptor1
        InterceptorDto interceptor1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptor1);
        // create model1
        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setInterceptors(List.of(interceptor1.getName()));
        modelFacade.createModel(modelDto);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = modelFacade.getAll();

        modelDto.setDescription("new description");
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        interceptorFacade.deleteInterceptor(interceptor1.getName());

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(actualAtRevision, modelsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackModelsWithAdapters() {
        initRoles();

        // create adapter1
        AdapterDto adapter1 = createAdapterDto("1");
        adapterFacade.createAdapter(adapter1);
        // create model1
        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setEndpoint(null);
        modelDto.setSource(new ModelAdapterSourceDto(adapter1.getName(), "/chat/completions"));
        modelFacade.createModel(modelDto);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = modelFacade.getAll();

        // create adapter2
        AdapterDto adapter2 = createAdapterDto("2");
        adapterFacade.createAdapter(adapter2);

        // update model
        modelDto.setSource(new ModelAdapterSourceDto(adapter2.getName(), "/chat/completions"));
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(actualAtRevision, modelsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackModelAfterRoleLimitRemoval() {
        // create role
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto modelDto = createModelDtoWithLimitsAndEndpoint("1");
        modelDto.setRoleLimits(Map.of("role1", limitDto));
        modelFacade.createModel(modelDto);

        // remember rev number and expected models state
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        Collection<ModelDto> actualAtRevision = modelFacade.getAll();

        // remove model role limit
        ModelDto updatedModel = createModelDtoWithLimitsAndEndpoint("1");
        updatedModel.setRoleLimits(null);
        modelFacade.updateModel(updatedModel.getName(), updatedModel, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(actualAtRevision, modelsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackModelAfterRoleLimitAddition() {
        // create role
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model
        ModelDto modelDto = createModelDto("1");
        modelFacade.createModel(modelDto);

        // remember rev number and expected models state
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        Collection<ModelDto> actualAtRevision = modelFacade.getAll();

        // add model role limit
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto updatedModel = createModelDto("1");
        updatedModel.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.updateModel(updatedModel.getName(), updatedModel, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(actualAtRevision, modelsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackModelAfterRoleLimitUpdate() {
        // create role
        RoleDto roleDto = createRoleDto("1");
        roleFacade.createRole(roleDto);

        // create model
        ModelDto modelDto = createModelDto("1");
        modelFacade.createModel(modelDto);

        // add model role limit
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        ModelDto updatedModel = createModelDto("1");
        updatedModel.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.updateModel(updatedModel.getName(), updatedModel, "*");

        // remember rev number and expected models state
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        Collection<ModelDto> actualAtRevision = modelFacade.getAll();

        // update model role limit
        limitDto = new LimitDto();
        limitDto.setDay(10L);
        limitDto.setWeek(20L);

        updatedModel = createModelDto("1");
        updatedModel.setRoleLimits(Map.of(roleDto.getName(), limitDto));
        modelFacade.updateModel(updatedModel.getName(), updatedModel, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(actualAtRevision, modelsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackDeletedModelWithAdapter() {
        // create adapter
        AdapterDto adapterDto = createAdapterDto("1");
        adapterFacade.createAdapter(adapterDto);

        // create model
        ModelDto modelDto = createModelDtoWithAdapter("1");
        modelFacade.createModel(modelDto);

        // remember rev number and expected models state
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        Collection<ModelDto> actualAtRevision = modelFacade.getAll();

        // delete model
        modelFacade.deleteModel(modelDto.getName());

        // rollback and verify
        int revisionsListSizeBeforeRollback = historyFacade.getRevisionsListSize();
        historyFacade.rollbackToRevision(revNumberToRollback);
        int revisionsListSizeAfterRollback = historyFacade.getRevisionsListSize();

        Assertions.assertEquals(revisionsListSizeBeforeRollback + 1, revisionsListSizeAfterRollback);

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(actualAtRevision, modelsAfterRollbackToRevision);
    }

    private void assertModel(ModelDto actual, ModelDto expected) {
        Assertions.assertEquals(expected, actual);
    }
}
