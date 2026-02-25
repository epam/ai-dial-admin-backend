package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.dto.source.InterceptorRunnerSourceDto;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorRunnerDto;

public abstract class InterceptorRunnerHistoryFunctionalTest {

    @Autowired
    private InterceptorRunnerFacade interceptorRunnerFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptorRunner() {

        // create interceptorRunner1
        InterceptorRunnerDto interceptorRunnerDto = createInterceptorRunnerDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        // update interceptorRunner1 description
        InterceptorRunnerDto updatedInterceptorRunner = createInterceptorRunnerDto("1");
        updatedInterceptorRunner.setDescription("new interceptorRunner description");
        interceptorRunnerFacade.updateInterceptorRunner(interceptorRunnerDto.getName(), updatedInterceptorRunner, "*");

        // verify interceptorRunner1
        InterceptorRunnerDto actual = interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerDto.getName());
        var expected = createInterceptorRunnerDto("1");
        expected.setDescription("new interceptorRunner description");
        assertInterceptorRunner(actual, expected);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        updatedInterceptorRunner.setDescription("new new interceptorRunner description");
        interceptorRunnerFacade.updateInterceptorRunner(interceptorRunnerDto.getName(), updatedInterceptorRunner, "*");

        // delete interceptorRunner 1
        interceptorRunnerFacade.deleteInterceptorRunner(interceptorRunnerDto.getName(), false);

        // create interceptorRunner 2
        interceptorRunnerFacade.createInterceptorRunner(createInterceptorRunnerDto("2"));

        // create interceptorRunner3
        interceptorRunnerFacade.createInterceptorRunner(createInterceptorRunnerDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<InterceptorRunnerDto> interceptorRunnersAfterRollbackToRevision = interceptorRunnerFacade.getAllInterceptorRunners();
        Assertions.assertEquals(List.of(actual), interceptorRunnersAfterRollbackToRevision);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    public void shouldSuccessfullyRollbackDeletedInterceptorRunnerWithInterceptor(boolean removeInterceptor) {
        // create interceptor runner
        InterceptorRunnerDto interceptorRunnerDto = createInterceptorRunnerDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        // create interceptor
        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setSource(new InterceptorRunnerSourceDto(interceptorRunnerDto.getName()));
        interceptorFacade.createInterceptor(interceptorDto);

        // remember rev number and expected interceptor runners state
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        Collection<InterceptorRunnerDto> actualAtRevision = interceptorRunnerFacade.getAllInterceptorRunners();

        // delete interceptor runner
        interceptorRunnerFacade.deleteInterceptorRunner(interceptorRunnerDto.getName(), removeInterceptor);

        // rollback and verify
        int revisionsListSizeBeforeRollback = historyFacade.getRevisionsListSize();
        historyFacade.rollbackToRevision(revNumberToRollback);
        int revisionsListSizeAfterRollback = historyFacade.getRevisionsListSize();

        Assertions.assertEquals(revisionsListSizeBeforeRollback + 1, revisionsListSizeAfterRollback);

        Collection<InterceptorRunnerDto> interceptorRunnersAfterRollbackToRevision = interceptorRunnerFacade.getAllInterceptorRunners();
        Assertions.assertEquals(actualAtRevision, interceptorRunnersAfterRollbackToRevision);
    }

    private void assertInterceptorRunner(InterceptorRunnerDto actual, InterceptorRunnerDto expected) {
        Assertions.assertEquals(expected, actual);
    }
}