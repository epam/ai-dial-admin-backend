package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.dto.source.InterceptorEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.InterceptorRunnerSourceDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class InterceptorRunnerFunctionalTest {

    @Autowired
    private InterceptorRunnerFacade interceptorRunnerFacade;
    
    @Autowired
    private InterceptorFacade interceptorFacade;

    @Test
    public void shouldSuccessfullyCreateAndGetInterceptorRunners() {
        String firstSuffix = "1";
        String secondSuffix = "2";

        // create interceptorRunner1
        InterceptorRunnerDto interceptorRunnerDto = createDto(firstSuffix);
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        InterceptorRunnerDto actual = interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerDto.getName());
        InterceptorRunnerDto expected1 = createDto(firstSuffix);

        assertInterceptorRunner(actual, expected1);

        // create interceptorRunner2
        InterceptorRunnerDto expected2 = createDto(secondSuffix);
        interceptorRunnerFacade.createInterceptorRunner(expected2);

        Collection<InterceptorRunnerDto> actualInterceptorRunners = interceptorRunnerFacade.getAllInterceptorRunners();
        assertInterceptorRunners(actualInterceptorRunners, List.of(expected1, expected2));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteInterceptorRunner() {
        String firstSuffix = "1";

        // create interceptorRunner1
        InterceptorRunnerDto interceptorRunnerDto = createDto(firstSuffix);
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        interceptorRunnerFacade.deleteInterceptorRunner(interceptorRunnerDto.getName(), false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerDto.getName()));
        Assertions.assertTrue(interceptorRunnerFacade.getAllInterceptorRunners().isEmpty());
    }
    
    @Test
    public void shouldSuccessfullyDeleteInterceptorRunnerAndInterceptors() {
        String runnerSuffix = "runner";
        String interceptorSuffix = "interceptor";

        InterceptorRunnerDto runnerDto = createDto(runnerSuffix);
        interceptorRunnerFacade.createInterceptorRunner(runnerDto);

        InterceptorDto interceptorDto = createInterceptorDto(interceptorSuffix, runnerDto.getName());
        interceptorFacade.createInterceptor(interceptorDto);

        InterceptorDto retrievedInterceptor = interceptorFacade.getInterceptor(interceptorDto.getName());
        Assertions.assertEquals(runnerDto.getName(), ((InterceptorRunnerSourceDto) retrievedInterceptor.getSource()).runnerName());

        interceptorRunnerFacade.deleteInterceptorRunner(runnerDto.getName(), true);

        Assertions.assertThrows(EntityNotFoundException.class, 
                () -> interceptorRunnerFacade.getInterceptorRunner(runnerDto.getName()));

        Assertions.assertThrows(EntityNotFoundException.class, 
                () -> interceptorFacade.getInterceptor(interceptorDto.getName()));
    }
    
    @Test
    public void shouldDetachInterceptorsWhenDeletingRunnerWithoutRemoveFlag() {
        String runnerSuffix = "detach";
        String interceptorSuffix = "detached";

        InterceptorRunnerDto runnerDto = createDto(runnerSuffix);
        interceptorRunnerFacade.createInterceptorRunner(runnerDto);

        InterceptorDto interceptorDto = createInterceptorDto(interceptorSuffix, runnerDto.getName());
        interceptorFacade.createInterceptor(interceptorDto);

        interceptorRunnerFacade.deleteInterceptorRunner(runnerDto.getName(), false);

        Assertions.assertThrows(EntityNotFoundException.class, 
                () -> interceptorRunnerFacade.getInterceptorRunner(runnerDto.getName()));

        InterceptorDto detachedInterceptor = interceptorFacade.getInterceptor(interceptorDto.getName());
        Assertions.assertTrue(detachedInterceptor.getSource() instanceof InterceptorEndpointsSourceDto);
        Assertions.assertEquals(runnerDto.getCompletionEndpoint(), detachedInterceptor.getEndpoint());
        Assertions.assertEquals(runnerDto.getConfigurationEndpoint(), detachedInterceptor.getConfigurationEndpoint());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateInterceptorRunner() {
        InterceptorRunnerDto interceptorRunnerDto = createDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        InterceptorRunnerDto updatedInterceptorRunner = createDto("1");
        updatedInterceptorRunner.setDescription("updated description");

        interceptorRunnerFacade.updateInterceptorRunner(interceptorRunnerDto.getName(), updatedInterceptorRunner);

        InterceptorRunnerDto actual = interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerDto.getName());
        var expected = createDto("1");
        expected.setDescription("updated description");
        assertInterceptorRunner(actual, expected);
    }

    @Test
    public void shouldThrowExceptionWhenCreateInterceptorRunnerWithExistingName() {
        InterceptorRunnerDto interceptorRunnerDto = createDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> interceptorRunnerFacade.createInterceptorRunner(createDto("1"))
        );

        Assertions.assertEquals("Interceptor Runner with name 'interceptorRunner1' already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateInterceptorRunnerWithExistingName() {
        InterceptorRunnerDto interceptorRunnerDto = createDto("1");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);

        InterceptorRunnerDto interceptorRunnerDto2 = createDto("2");
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto2);

        interceptorRunnerDto.setName("interceptorRunner2");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> interceptorRunnerFacade.updateInterceptorRunner("interceptorRunner1", interceptorRunnerDto)
        );

        Assertions.assertEquals(
                "Interceptor runner with name: 'interceptorRunner1' can not be renamed. New interceptor runner name: 'interceptorRunner2'",
                exception.getMessage()
        );
    }

    private InterceptorRunnerDto createDto(String suffix) {
        InterceptorRunnerDto interceptorRunnerDto = new InterceptorRunnerDto();
        interceptorRunnerDto.setName("interceptorRunner" + suffix);
        interceptorRunnerDto.setDisplayName("Interceptor Runner " + suffix);
        interceptorRunnerDto.setDescription("description" + suffix);
        interceptorRunnerDto.setCompletionEndpoint("https://endpoint.test.com/completion" + suffix);
        interceptorRunnerDto.setConfigurationEndpoint("https://endpoint.test.com/configuration" + suffix);
        return interceptorRunnerDto;
    }
    
    private InterceptorDto createInterceptorDto(String suffix, String runnerName) {
        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("interceptor" + suffix);
        interceptorDto.setDescription("description" + suffix);
        interceptorDto.setSource(new InterceptorRunnerSourceDto(runnerName));
        return interceptorDto;
    }

    private void assertInterceptorRunner(InterceptorRunnerDto actual, InterceptorRunnerDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDisplayName(), actual.getDisplayName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        Assertions.assertEquals(expected.getCompletionEndpoint(), actual.getCompletionEndpoint());
        Assertions.assertEquals(expected.getConfigurationEndpoint(), actual.getConfigurationEndpoint());
    }

    private Map<String, InterceptorRunnerDto> toMap(Collection<InterceptorRunnerDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(InterceptorRunnerDto::getName, Function.identity()));
    }

    private void assertInterceptorRunners(Collection<InterceptorRunnerDto> actual, Collection<InterceptorRunnerDto> expected) {
        Map<String, InterceptorRunnerDto> actualMap = toMap(actual);
        Map<String, InterceptorRunnerDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertInterceptorRunner(actualMap.get(name), expectedMap.get(name));
        }
    }
}