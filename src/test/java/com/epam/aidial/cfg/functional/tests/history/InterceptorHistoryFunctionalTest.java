package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
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

public abstract class InterceptorHistoryFunctionalTest {

    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptor() {

        // create interceptor1
        InterceptorDto interceptorDto = createDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        // update interceptor1 description
        InterceptorDto updatedInterceptor = createDto("1");
        updatedInterceptor.setDescription("new interceptor description");
        interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor);

        // verify interceptor1
        InterceptorDto actual = interceptorFacade.getInterceptor(interceptorDto.getName());
        var expected = createDto("1");
        expected.setDescription("new interceptor description");
        assertInterceptor(actual, expected);

        var actualAtRevision = actual;
        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        updatedInterceptor.setDescription("new new interceptor description");
        interceptorFacade.updateInterceptor(interceptorDto.getName(), updatedInterceptor);

        // delete interceptor 1
        interceptorFacade.deleteInterceptor(interceptorDto.getName());

        // create interceptor 2
        interceptorFacade.createInterceptor(createDto("2"));


        // create interceptor3
        interceptorFacade.createInterceptor(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<InterceptorDto> interceptorsAfterRollbackToRevision = interceptorFacade.getAllInterceptors();
        Assertions.assertEquals(List.of(actualAtRevision), interceptorsAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackModelsWithModels() {
        initRoles();

        // create model1
        ModelDto model1 = createModel("1");
        modelFacade.createModel(model1);
        ModelDto model2 = createModel("2");
        modelFacade.createModel(model2);

        // create interceptor1
        InterceptorDto interceptor1 = createDto("1");
        interceptor1.setEntities(List.of(model1.getName()));
        interceptorFacade.createInterceptor(interceptor1);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = interceptorFacade.getAllInterceptors();

        // update interceptor
        interceptor1.setEntities(List.of(model2.getName()));
        interceptorFacade.updateInterceptor(interceptor1.getName(), interceptor1);

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
        ApplicationDto application1 = createApplication("1");
        application1.setEndpoint("endpoint1");
        applicationFacade.createApplication(application1);
        ApplicationDto application2 = createApplication("2");
        application2.setEndpoint("endpoint2");
        applicationFacade.createApplication(application2);

        // create interceptor1
        InterceptorDto interceptor1 = createDto("1");
        interceptor1.setEntities(List.of(application1.getName()));
        interceptorFacade.createInterceptor(interceptor1);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = interceptorFacade.getAllInterceptors();

        // update interceptor
        interceptor1.setEntities(List.of(application2.getName()));
        interceptorFacade.updateInterceptor(interceptor1.getName(), interceptor1);

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        var interceptorsAfterRollbackToRevision = interceptorFacade.getAllInterceptors();
        Assertions.assertEquals(actualAtRevision, interceptorsAfterRollbackToRevision);
    }

    private ModelDto createModel(String suffix) {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model" + suffix);
        modelDto.setDescription("description" + suffix);
        modelDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return modelDto;
    }

    private ApplicationDto createApplication(String suffix) {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("model" + suffix);
        applicationDto.setDescription("description" + suffix);
        applicationDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return applicationDto;
    }

    private void initRoles() {
        RoleDto role1 = new RoleDto();
        role1.setName("role1");
        role1.setDescription("role1");
        RoleDto role2 = new RoleDto();
        role2.setName("role2");
        role2.setDescription("role2");
        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
    }


    private void assertInterceptor(InterceptorDto actual, InterceptorDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private InterceptorDto createDto(String suffix) {
        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("interceptor" + suffix);
        interceptorDto.setDescription("description" + suffix);
        interceptorDto.setEntities(List.of());
        return interceptorDto;
    }
}
