package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.source.AdapterSourceDto;
import com.epam.aidial.cfg.dto.source.ModelEndpointsSourceDto;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.AuditActivityFacade;
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

public abstract class ModelHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private TestHistoryFacade historyFacade;
    @Autowired
    private AuditActivityFacade auditActivityFacade;
    @Autowired
    private AdapterFacade adapterFacade;

    private void initRoles() {
        RoleDto role1 = createRoleDto("1");
        RoleDto role2 = createRoleDto("2");
        RoleDto role3 = createRoleDto("3");
        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
        roleFacade.createRole(role3);
    }

    @Test
    public void shouldSuccessfullyRollbackModels() {
        initRoles();

        // create model1
        ModelDto modelDto = createDto("1");
        modelFacade.createModel(modelDto);

        // update model1 description
        ModelDto updatedModel = createDto("1");
        updatedModel.setDescription("new model description");
        updatedModel.setDefaults(Map.of());
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");

        // verify model1
        ModelDto actual = modelFacade.getModel(modelDto.getName());
        var expected = createDto("1");
        expected.setDescription("new model description");
        expected.setDefaults(Map.of());
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        expected.setMaxRetryAttempts(1);
        assertModel(actual, expected);

        // add roles to model1
        updatedModel.setMaxRetryAttempts(3);
        updatedModel.setDefaults(Map.of());
        updatedModel.setDefaultRoleLimit(new LimitDto());
        updatedModel.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        updatedModel.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        updatedModel.setRoleShareResourceLimits(Map.of("role2", new ShareResourceLimitDto(), "role3", new ShareResourceLimitDto()));
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");
        actual = modelFacade.getModel(modelDto.getName());
        assertModel(actual, updatedModel);

        // update model1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(20L);
        updatedModel.setRoleLimits(Map.of("role3", limitDto));
        updatedModel.setRoleShareResourceLimits(Map.of("role3", shareResourceLimitDto));
        modelFacade.updateModel(modelDto.getName(), updatedModel, "*");
        var actualAtRevision = modelFacade.getModel(modelDto.getName());
        assertModel(actualAtRevision, updatedModel);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // delete role3
        roleFacade.deleteRole("role3");
        actual = modelFacade.getModel(modelDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());
        Assertions.assertTrue(actual.getRoleShareResourceLimits().isEmpty());

        // delete model 1
        modelFacade.deleteModel(modelDto.getName());

        // create model 2
        modelFacade.createModel(createDto("2"));

        // create role3
        RoleDto role3 = new RoleDto();
        role3.setName("role3");
        role3.setDescription("role3");
        roleFacade.createRole(role3);

        // create model3 with assigned role3
        modelFacade.createModel(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(List.of(actualAtRevision), modelsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackModelsWithInterceptors() {
        initRoles();

        // create interceptor1
        InterceptorDto interceptor1 = createInterceptor("1");
        interceptorFacade.createInterceptor(interceptor1);
        // create model1
        ModelDto modelDto = createDto("1");
        modelDto.setInterceptors(List.of(interceptor1.getName()));
        modelFacade.createModel(modelDto);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = modelFacade.getAll();

        // create interceptor1
        InterceptorDto interceptor2 = createInterceptor("2");
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
        InterceptorDto interceptor1 = createInterceptor("1");
        interceptorFacade.createInterceptor(interceptor1);
        // create model1
        ModelDto modelDto = createDto("1");
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
        AdapterDto adapter1 = createAdapter("1");
        adapterFacade.createAdapter(adapter1);
        // create model1
        ModelDto modelDto = createDto("1");
        modelDto.setEndpoint(null);
        modelDto.setSource(new AdapterSourceDto(adapter1.getName(), "/chat/completions"));
        modelFacade.createModel(modelDto);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = modelFacade.getAll();

        // create adapter2
        AdapterDto adapter2 = createAdapter("2");
        adapterFacade.createAdapter(adapter2);

        // update model
        modelDto.setSource(new AdapterSourceDto(adapter2.getName(), "/chat/completions"));
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ModelDto> modelsAfterRollbackToRevision = modelFacade.getAll();
        Assertions.assertEquals(actualAtRevision, modelsAfterRollbackToRevision);
    }

    private InterceptorDto createInterceptor(String suffix) {
        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("interceptor" + suffix);
        interceptorDto.setDescription("int description" + suffix);
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor" + suffix);
        return interceptorDto;
    }

    private void assertModel(ModelDto actual, ModelDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private ModelDto createDto(String suffix) {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model" + suffix);
        modelDto.setDescription("description" + suffix);
        modelDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        modelDto.setRoleShareResourceLimits(Map.of(
                "role" + suffix, new ShareResourceLimitDto()
        ));
        modelDto.setSource(new ModelEndpointsSourceDto());
        modelDto.setEndpoint("https://endpoint1/chat/completions");
        return modelDto;
    }

    private RoleDto createRoleDto(String suffix) {
        RoleDto role1 = new RoleDto();
        role1.setName("role" + suffix);
        role1.setDescription("role" + suffix);
        return role1;
    }

    private AdapterDto createAdapter(String suffix) {
        AdapterDto adapterDto = new AdapterDto();
        adapterDto.setName("adapter" + suffix);
        adapterDto.setBaseEndpoint("adapter" + suffix + "/endpoint");
        adapterDto.setDescription("adapter" + suffix);
        return adapterDto;
    }
}
