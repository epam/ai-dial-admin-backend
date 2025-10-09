package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class InterceptorRunnerHistoryFunctionalTest {

    @Autowired
    private InterceptorRunnerFacade interceptorRunnerFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptorRunner() {

        // create interceptorRunner1
        InterceptorRunnerDto interceptorRunnerDto = createDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        // update interceptorRunner1 description
        InterceptorRunnerDto updatedInterceptorRunner = createDto("1");
        updatedInterceptorRunner.setDescription("new interceptorRunner description");
        interceptorRunnerFacade.updateInterceptorRunner(interceptorRunnerDto.getName(), updatedInterceptorRunner, "*");

        // verify interceptorRunner1
        InterceptorRunnerDto actual = interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerDto.getName());
        var expected = createDto("1");
        expected.setDescription("new interceptorRunner description");
        assertInterceptorRunner(actual, expected);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        updatedInterceptorRunner.setDescription("new new interceptorRunner description");
        interceptorRunnerFacade.updateInterceptorRunner(interceptorRunnerDto.getName(), updatedInterceptorRunner, "*");

        // delete interceptorRunner 1
        interceptorRunnerFacade.deleteInterceptorRunner(interceptorRunnerDto.getName(), false);

        // create interceptorRunner 2
        interceptorRunnerFacade.createInterceptorRunner(createDto("2"));

        // create interceptorRunner3
        interceptorRunnerFacade.createInterceptorRunner(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<InterceptorRunnerDto> interceptorRunnersAfterRollbackToRevision = interceptorRunnerFacade.getAllInterceptorRunners();
        Assertions.assertEquals(List.of(actual), interceptorRunnersAfterRollbackToRevision);
    }

    private void assertInterceptorRunner(InterceptorRunnerDto actual, InterceptorRunnerDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private InterceptorRunnerDto createDto(String suffix) {
        InterceptorRunnerDto interceptorRunnerDto = new InterceptorRunnerDto();
        interceptorRunnerDto.setName("interceptorRunner" + suffix);
        interceptorRunnerDto.setDisplayName("Interceptor Runner " + suffix);
        interceptorRunnerDto.setDescription("description" + suffix);
        interceptorRunnerDto.setCompletionEndpoint("https://endpoint.test.com/completion" + suffix);
        interceptorRunnerDto.setConfigurationEndpoint("https://endpoint.test.com/configuration" + suffix);
        interceptorRunnerDto.setInterceptors(new ArrayList<>());
        return interceptorRunnerDto;
    }
}