package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ApplicationFunctionalTest {

    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private RoleFacade roleFacade;

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

    @Test
    public void shouldSuccessfullyCreateAndGetApplication() {
        initRoles();
        ApplicationDto applicationDto = createDtoWithDefaults("1");

        applicationFacade.createApplication(applicationDto);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());
        ApplicationDto expected = createDtoWithDefaults("1");

        assertApplicationWithDefaults(actual, expected);

        applicationFacade.createApplication(createDto("2"));

        Collection<ApplicationInfoDto> actualApplications = applicationFacade.getAllApplications();

        assertApp(actualApplications, List.of(createDto("1"), createDto("2")));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteApplication() {
        initRoles();
        ApplicationDto applicationDto = createDto("1");
        applicationFacade.createApplication(applicationDto);

        applicationFacade.deleteApplication(applicationDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> applicationFacade.getApplication(applicationDto.getName()));
        Assertions.assertTrue(applicationFacade.getAllApplications().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplication() {
        initRoles();
        ApplicationDto applicationDto = createDto("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createDto("1");
        updatedApplication.setDescription("new application description");

        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());
        var expected = createDto("1");
        expected.setDescription("new application description");
        assertApplication(actual, expected);
    }

    @Test
    public void shouldSuccessfullyCreateAndAddInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("int1");
        interceptorDto.setDescription("int1_dsc");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        interceptorFacade.createInterceptor(interceptorDto);

        ApplicationDto applicationDto = createDto("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createDto("1");

        updatedApplication.setDescription("new model description");
        updatedApplication.setDefaults(Map.of());
        updatedApplication.setInterceptors(List.of("int1"));

        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("int1"));
    }

    @Test
    public void shouldThrowExceptionWhenRenameApplication() {
        initRoles();
        ApplicationDto applicationDto = createDto("1");
        applicationFacade.createApplication(applicationDto);
        ApplicationDto updatedApplication = createDto("2");
        updatedApplication.setDescription("new application description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), updatedApplication)
        );
        Assertions.assertEquals("Application with name: 'application1' can not be renamed. New name: 'application2'", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyCreateWithInterceptor() {
        initRoles();

        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("int1");
        interceptorDto.setDescription("int1_dsc");
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor");
        interceptorFacade.createInterceptor(interceptorDto);

        ApplicationDto applicationDto = createDto("1");
        applicationDto.setInterceptors(List.of("int1"));
        applicationFacade.createApplication(applicationDto);

        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());

        Assertions.assertTrue(actual.getInterceptors().contains("int1"));
    }

    @Test
    public void shouldThrowExceptionWhenCreateApplicationWithExistingDisplayNameAndDisplayVersion() {
        initRoles();

        ApplicationDto applicationDto = createDto("1");
        applicationDto.setDisplayName("display_name");
        applicationDto.setDisplayVersion("1.0");
        applicationFacade.createApplication(applicationDto);

        ApplicationDto applicationDto2 = createDto("2");
        applicationDto2.setDisplayName("display_name");
        applicationDto2.setDisplayVersion("1.0");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> applicationFacade.createApplication(applicationDto2)
        );
        Assertions.assertEquals("Application with display name: 'display_name' and display version: '1.0' already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateApplicationWithExistingDisplayNameAndDisplayVersion() {
        initRoles();

        ApplicationDto applicationDto = createDto("1");
        applicationDto.setDisplayName("display_name");
        applicationFacade.createApplication(applicationDto);

        ApplicationDto applicationDto2 = createDto("2");
        applicationDto2.setDisplayName("display_name_2");
        applicationFacade.createApplication(applicationDto2);

        applicationDto.setDisplayName("display_name_2");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> applicationFacade.updateApplication(applicationDto.getName(), applicationDto)
        );
        Assertions.assertEquals("Application with display name: 'display_name_2' and display version: 'null' already exists", exception.getMessage());
    }

    private ApplicationDto createDto(String suffix) {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("application" + suffix);
        applicationDto.setDescription("description" + suffix);
        applicationDto.setEndpoint("endpoint");
        applicationDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return applicationDto;
    }

    private ApplicationDto createDtoWithDefaults(String suffix) {
        ApplicationDto applicationDto = createDto(suffix);
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
