package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.GlobalSettingsDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.source.InterceptorEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.InterceptorRunnerSourceDto;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.GlobalSettingsFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpoint;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorRunnerDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;

public abstract class InterceptorHistoryFunctionalTest {

    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private GlobalSettingsFacade globalSettingsFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private InterceptorRunnerFacade interceptorRunnerFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptor() {

        // create interceptor1
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        // update interceptor1 description
        InterceptorDto updatedInterceptor = createInterceptorDto("1");
        updatedInterceptor.setDescription("new interceptor description");
        interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor, "*");

        // verify interceptor1
        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        var expected = createInterceptorDto("1");
        expected.setDescription("new interceptor description");
        expected.setSource(new InterceptorEndpointsSourceDto());
        expected.setApplicationTypeSchemas(List.of());
        assertInterceptor(actual, expected);

        var actualAtRevision = actual;
        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        updatedInterceptor.setDescription("new new interceptor description");
        updatedInterceptor.setDefaults(Map.of("key1", "val1"));
        interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor, "*");

        // delete interceptor 1
        interceptorFacade.deleteInterceptor(interceptorDto.getName());

        // create interceptor 2
        interceptorFacade.createInterceptor(createInterceptorDto("2"));


        // create interceptor3
        interceptorFacade.createInterceptor(createInterceptorDto("3"));

        // create global interceptors
        var globalSettings = new GlobalSettingsDto();
        globalSettings.setGlobalInterceptors(List.of("interceptor3", "interceptor2"));
        globalSettingsFacade.updateGlobalSettings(globalSettings);
        Assertions.assertEquals(List.of("interceptor3", "interceptor2"),
                globalSettingsFacade.getGlobalSettings().getGlobalInterceptors());

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<InterceptorDto> interceptorsAfterRollbackToRevision = interceptorFacade.getAllInterceptors();
        Assertions.assertEquals(List.of(actualAtRevision), interceptorsAfterRollbackToRevision);
        Assertions.assertEquals(List.of(), globalSettingsFacade.getGlobalSettings().getGlobalInterceptors());
    }

    @Test
    public void shouldSuccessfullyRollbackModelsWithModels() {
        initRoles();

        // create model1
        ModelDto model1 = createModelDto("1");
        modelFacade.createModel(model1);
        ModelDto model2 = createModelDto("2");
        modelFacade.createModel(model2);

        // create interceptor1
        InterceptorDto interceptor1 = createInterceptorDto("1");
        interceptor1.setEntities(List.of(model1.getName()));
        interceptorFacade.createInterceptor(interceptor1);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = interceptorFacade.getAllInterceptors();

        // update interceptor
        interceptor1.setEntities(List.of(model2.getName()));
        interceptorFacade.updateInterceptor(interceptor1.getName(), interceptor1, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        var interceptorsAfterRollbackToRevision = interceptorFacade.getAllInterceptors();
        Assertions.assertEquals(actualAtRevision, interceptorsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackModelsWithApplications() {
        initRoles();

        // create application1
        ApplicationDto application1 = createApplicationDtoWithEndpoint("1");
        applicationFacade.createApplication(application1);
        ApplicationDto application2 = createApplicationDtoWithEndpoint("2");
        applicationFacade.createApplication(application2);

        // create interceptor1
        InterceptorDto interceptor1 = createInterceptorDto("1");
        interceptor1.setEntities(List.of(application1.getName()));
        interceptorFacade.createInterceptor(interceptor1);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = interceptorFacade.getAllInterceptors();

        // update interceptor
        interceptor1.setEntities(List.of(application2.getName()));
        interceptorFacade.updateInterceptor(interceptor1.getName(), interceptor1, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        var interceptorsAfterRollbackToRevision = interceptorFacade.getAllInterceptors();
        Assertions.assertEquals(actualAtRevision, interceptorsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackDeletedInterceptorWithInterceptorRunner() {
        // create interceptor runner
        InterceptorRunnerDto interceptorRunnerDto = createInterceptorRunnerDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        // create interceptor
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setSource(new InterceptorRunnerSourceDto(interceptorRunnerDto.getName()));
        interceptorFacade.createInterceptor(interceptorDto);

        // remember rev number and expected interceptors state
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        Collection<InterceptorDto> actualAtRevision = interceptorFacade.getAllInterceptors();

        // delete interceptor
        interceptorFacade.deleteInterceptor(interceptorDto.getName());

        // rollback and verify
        int revisionsListSizeBeforeRollback = historyFacade.getRevisionsListSize();
        historyFacade.rollbackToRevision(revNumberToRollback);
        int revisionsListSizeAfterRollback = historyFacade.getRevisionsListSize();

        Assertions.assertEquals(revisionsListSizeBeforeRollback + 1, revisionsListSizeAfterRollback);

        Collection<InterceptorDto> interceptorsAfterRollbackToRevision = interceptorFacade.getAllInterceptors();
        Assertions.assertEquals(actualAtRevision, interceptorsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackInterceptorsWithInterceptorRunner() {
        initRoles();

        // create interceptor runner 1
        InterceptorRunnerDto interceptorRunnerDto = createInterceptorRunnerDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        // create interceptor
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setSource(new InterceptorRunnerSourceDto(interceptorRunnerDto.getName()));
        interceptorFacade.createInterceptor(interceptorDto);

        // remember rev number and expected interceptors state
        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = interceptorFacade.getAllInterceptors();

        // create interceptor runner 2
        InterceptorRunnerDto interceptorRunnerDto2 = createInterceptorRunnerDto("2");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto2);

        // update interceptor
        interceptorDto.setSource(new InterceptorRunnerSourceDto(interceptorRunnerDto2.getName()));
        interceptorFacade.updateInterceptor(interceptorDto.getName(), interceptorDto, "*");

        int revisionsListSizeBeforeRollback = historyFacade.getRevisionsListSize();
        historyFacade.rollbackToRevision(revNumberToRollback);
        int revisionsListSizeAfterRollback = historyFacade.getRevisionsListSize();

        Assertions.assertEquals(revisionsListSizeBeforeRollback + 1, revisionsListSizeAfterRollback);

        Collection<InterceptorDto> interceptorsAfterRollbackToRevision = interceptorFacade.getAllInterceptors();
        Assertions.assertEquals(actualAtRevision, interceptorsAfterRollbackToRevision);
    }

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
    }

    private void assertInterceptor(InterceptorDto actual, InterceptorDto expected) {
        Assertions.assertEquals(expected, actual);
    }
}