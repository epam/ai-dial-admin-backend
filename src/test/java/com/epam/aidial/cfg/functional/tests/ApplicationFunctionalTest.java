package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.core.config.CoreApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpointAndLimits;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createBaseApplicationDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.defaultCoreFeatures;

public abstract class ApplicationFunctionalTest {

    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private RoleFacade roleFacade;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetApplication() {
        initRoles();
        ApplicationDto applicationDto = createDtoWithDefaults("1");

        applicationFacade.createApplication(applicationDto);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());
        ApplicationDto expected = createDtoWithDefaults("1");

        assertApplicationWithDefaults(actual, expected);

        applicationFacade.createApplication(createApplicationDtoWithEndpointAndLimits("2"));

        Collection<ApplicationInfoDto> actualApplications = applicationFacade.getAllApplications();

        assertApp(actualApplications, List.of(createApplicationDtoWithEndpointAndLimits("1"), createApplicationDtoWithEndpointAndLimits("2")));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteApplication() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        applicationFacade.deleteApplication(applicationDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> applicationFacade.getApplication(applicationDto.getName()));
        Assertions.assertTrue(applicationFacade.getAllApplications().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplication() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("1");
        updatedApplication.setDescription("new application description");

        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*");

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());
        var expected = createApplicationDtoWithEndpointAndLimits("1");
        expected.setDescription("new application description");
        assertApplication(actual, expected);
    }

    @Test
    public void shouldSuccessfullyCreateAndAddInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto);

        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("1");

        updatedApplication.setDescription("new model description");
        updatedApplication.setDefaults(Map.of());
        updatedApplication.setInterceptors(List.of("interceptor1"));

        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*");

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("interceptor1"));
    }

    @Test
    public void shouldThrowExceptionWhenRenameApplication() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("2");
        updatedApplication.setDescription("new application description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*")
        );
        Assertions.assertEquals("Application with name: 'application1' can not be renamed. New name: 'application2'", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenApplicationConcurrencyOverwrite() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "test")
        );
        Assertions.assertEquals("Unable to update Application 'application1'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), applicationDto, null)
        );
        Assertions.assertEquals("Hash must not be null. Use \"*\" to skip optimistic check. Application:application1.",
                exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyUpdateApplicationWithCorrectHash() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("1");
        updatedApplication.setDescription("new application description");

        var hash = applicationFacade.getApplicationWithHash(applicationDto.getName()).hash();

        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, hash);

        var actual = applicationFacade.getApplication(applicationDto.getName());
        var expected = createApplicationDtoWithEndpointAndLimits("1");
        expected.setDescription("new application description");
        assertApplication(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateApplicationWithIncorrectHash() {
        initRoles();
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        Assertions.assertThrows(OptimisticLockConflictException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "test"));
    }

    @Test
    public void shouldSuccessfullyCreateWithInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = createInterceptorDto("1");
        interceptorDto.setName("int1");
        interceptorDto.setDescription("int1_dsc");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        interceptorFacade.createInterceptor(interceptorDto);

        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto.setInterceptors(List.of("int1"));
        applicationFacade.createApplication(applicationDto);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("int1"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateWithInterceptors() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor1", "interceptor1", "interceptor2"));
        applicationFacade.createApplication(applicationDto);

        ApplicationDto actualApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor2", "interceptor1", "interceptor1", "interceptor2"), actualApplication.getInterceptors());

        applicationDto.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"));
        applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "*");

        actualApplication = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"), actualApplication.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyAddNewInterceptorToTheEndOfTheInterceptorsList() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ApplicationDto applicationDto1 = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto1.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1"));
        applicationFacade.createApplication(applicationDto1);

        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor2"));
        applicationFacade.createApplication(applicationDto2);

        InterceptorDto interceptorDto3 = createInterceptorDto("3");
        interceptorDto3.setEntities(List.of("application1", "application2", "application1"));
        interceptorFacade.createInterceptor(interceptorDto3);

        ApplicationDto actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1", "interceptor3"), actualApplication1.getInterceptors());

        ApplicationDto actualApplication2 = applicationFacade.getApplication(applicationDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor2", "interceptor2", "interceptor3"), actualApplication2.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyRemoveDeletedInterceptorFromTheInterceptorsList() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        InterceptorDto interceptorDto3 = createInterceptorDto("3");
        interceptorFacade.createInterceptor(interceptorDto3);

        ApplicationDto applicationDto1 = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto1.setInterceptors(List.of("interceptor2", "interceptor2", "interceptor1", "interceptor1", "interceptor3"));
        applicationFacade.createApplication(applicationDto1);

        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setInterceptors(List.of("interceptor1", "interceptor2", "interceptor2", "interceptor3"));
        applicationFacade.createApplication(applicationDto2);

        interceptorFacade.deleteInterceptor("interceptor1");

        ApplicationDto actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor3"), actualApplication1.getInterceptors());

        ApplicationDto actualApplication2 = applicationFacade.getApplication(applicationDto2.getName());
        Assertions.assertEquals(List.of("interceptor2", "interceptor2", "interceptor3"), actualApplication2.getInterceptors());
    }

    @Test
    public void shouldSuccessfullyRemoveUpdatedInterceptorFromTheInterceptorsList() {
        initRoles();

        InterceptorDto interceptorDto1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptorDto1);

        InterceptorDto interceptorDto2 = createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptorDto2);

        ApplicationDto applicationDto1 = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto1.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        applicationFacade.createApplication(applicationDto1);

        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setInterceptors(List.of("interceptor1", "interceptor1", "interceptor2"));
        applicationFacade.createApplication(applicationDto2);

        interceptorDto1.setEntities(List.of("application2"));
        interceptorFacade.updateInterceptor(interceptorDto1.getName(), interceptorDto1, "*");

        ApplicationDto actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(List.of("interceptor2"), actualApplication1.getInterceptors());

        ApplicationDto actualApplication2 = applicationFacade.getApplication(applicationDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor1", "interceptor2"), actualApplication2.getInterceptors());

        interceptorDto2.setEntities(null);
        interceptorFacade.updateInterceptor(interceptorDto2.getName(), interceptorDto2, "*");

        actualApplication1 = applicationFacade.getApplication(applicationDto1.getName());
        Assertions.assertEquals(actualApplication1.getInterceptors(), List.of());

        actualApplication2 = applicationFacade.getApplication(applicationDto2.getName());
        Assertions.assertEquals(List.of("interceptor1", "interceptor1"), actualApplication2.getInterceptors());
    }

    @Test
    public void shouldSaveAndReturnApplicationWithUniqueTopics() {
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setEndpoint("http://my-endpoint");
        applicationDto.setTopics(List.of("topic1", "topic2", "topic1", "topic3", "topic2"));
        applicationFacade.createApplication(applicationDto);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());

        Assertions.assertEquals(List.of("topic1", "topic2", "topic3"), actual.getTopics());
    }

    @Test
    public void shouldSuccessfullyGetCoreApplication() {
        initRoles();

        ApplicationDto applicationDto = createDtoWithDefaults("1");
        applicationFacade.createApplication(applicationDto);

        CoreApplication expected = new CoreApplication();
        expected.setName(applicationDto.getName());
        expected.setDisplayName(applicationDto.getDisplayName());
        expected.setDescription(applicationDto.getDescription());
        expected.setEndpoint(applicationDto.getEndpoint());
        expected.setDefaults(applicationDto.getDefaults());
        expected.setApplicationProperties(applicationDto.getApplicationProperties());
        expected.setFeatures(defaultCoreFeatures());
        expected.setUserRoles(applicationDto.getRoleLimits().keySet());

        CoreApplication actual = applicationFacade.getCoreApplicationWithHash(applicationDto.getName()).core();
        actual.setCreatedAt(null);
        actual.setUpdatedAt(null);

        Assertions.assertEquals(expected, actual);
    }

    private ApplicationDto createDtoWithDefaults(String suffix) {
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits(suffix);
        applicationDto.setDefaults(Map.of("max_tokens", 8000));
        return applicationDto;
    }

    private void assertApplication(ApplicationDto actual, ApplicationDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        Assertions.assertEquals(expected.getRoleLimits(), actual.getRoleLimits());
    }

    private void assertApplicationWithDefaults(ApplicationDto actual, ApplicationDto expected) {
        assertApplication(actual, expected);
        Assertions.assertEquals(expected.getDefaults(), actual.getDefaults());
    }

    private void assertApp(Collection<ApplicationInfoDto> actual, Collection<ApplicationDto> expected) {
        Map<String, ApplicationInfoDto> actualMap = toMap(actual, ApplicationInfoDto::getName);
        Map<String, ApplicationDto> expectedMap = toMap(expected, ApplicationDto::getName);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertApp(actualMap.get(name), expectedMap.get(name));
        }
    }

    private void assertApp(ApplicationInfoDto actual, ApplicationDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
    }

    private <T> Map<String, T> toMap(Collection<T> dtos, Function<T, String> getName) {
        return dtos.stream()
                .collect(Collectors.toMap(getName, Function.identity()));
    }
}
