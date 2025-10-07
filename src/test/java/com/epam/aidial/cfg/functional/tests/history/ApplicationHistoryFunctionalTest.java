package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createApplicationDtoWithEndpointAndLimits;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createBaseApplicationDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createInterceptorDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;

public abstract class ApplicationHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ApplicationTypeSchemaFacade applicationTypeSchemaFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
        roleFacade.createRole(createRoleDto("3"));
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplication() {
        initRoles();

        // 1 create application1
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationFacade.createApplication(applicationDto);

        // 2 update application1 description
        ApplicationDto updatedApplication = createApplicationDtoWithEndpointAndLimits("1");
        updatedApplication.setDescription("new application description");
        updatedApplication.setEndpoint("endpoint2");
        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*");

        // verify application1
        ApplicationDto actual = applicationFacade.getApplication(applicationDto.getName());
        var expected = createApplicationDtoWithEndpointAndLimits("1");
        ShareResourceLimitDto defaultShareResourceLimitDto = new ShareResourceLimitDto();
        defaultShareResourceLimitDto.setMaxAcceptedUsers(10);
        expected.setDescription("new application description");
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setDefaults(Map.of());
        expected.setInterceptors(List.of());
        expected.setEndpoint("endpoint2");
        expected.setRoutes(List.of());
        expected.setMaxRetryAttempts(1);
        assertApplication(actual, expected);

        // 3 add roles to application1
        updatedApplication.setMaxRetryAttempts(3);
        updatedApplication.setDefaultRoleLimit(new LimitDto());
        updatedApplication.setDefaults(Map.of());
        updatedApplication.setInterceptors(List.of());
        updatedApplication.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        updatedApplication.setRoutes(List.of());
        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*");
        actual = applicationFacade.getApplication(applicationDto.getName());
        assertApplication(actual, updatedApplication);

        // 4 update application1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(20L);
        updatedApplication.setRoleLimits(Map.of("role3", limitDto));
        applicationFacade.updateApplication(applicationDto.getName(), updatedApplication, "*");
        var actualAtOldRevision = applicationFacade.getAllApplications();
        actual = applicationFacade.getApplication(applicationDto.getName());
        assertApplication(actual, updatedApplication);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // 5 delete role3
        roleFacade.deleteRole("role3");
        actual = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());

        // 6 delete application 1
        applicationFacade.deleteApplication(applicationDto.getName());

        // 7 create application 2
        ApplicationDto applicationDto2 = createApplicationDtoWithEndpointAndLimits("2");
        applicationDto2.setEndpoint("endpoint3");
        applicationFacade.createApplication(applicationDto2);

        // 8 create role3
        roleFacade.createRole(createRoleDto("3"));

        // 9 create application3 with assigned role3
        ApplicationDto applicationDto3 = createApplicationDtoWithEndpointAndLimits("3");
        applicationDto3.setEndpoint("endpoint4");
        applicationFacade.createApplication(applicationDto3);

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ApplicationInfoDto> applicationsAfterRollback = applicationFacade.getAllApplications();
        Assertions.assertEquals(actualAtOldRevision, applicationsAfterRollback);
    }

    @Test
    public void shouldSuccessfullyRollbackApplicationsWithInterceptors() {
        initRoles();

        // create interceptor1
        InterceptorDto interceptor1 = createInterceptorDto("1");
        interceptorFacade.createInterceptor(interceptor1);
        // create application1
        ApplicationDto applicationDto = createApplicationDtoWithEndpointAndLimits("1");
        applicationDto.setInterceptors(List.of(interceptor1.getName()));
        applicationFacade.createApplication(applicationDto);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = applicationFacade.getAllApplications();
        var actualApplicationAtRevision = applicationFacade.getApplication(applicationDto.getName());

        // create interceptor1
        InterceptorDto interceptor2 =  createInterceptorDto("2");
        interceptorFacade.createInterceptor(interceptor2);

        // update application
        applicationDto.setInterceptors(List.of(interceptor2.getName()));
        applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ApplicationInfoDto> applicationsAfterRollbackToRevision = applicationFacade.getAllApplications();
        var applicationAfterRollbackToRevision = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertEquals(actualAtRevision, applicationsAfterRollbackToRevision);
        Assertions.assertEquals(actualApplicationAtRevision, applicationAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackApplicationsWithAppTypeSchemas() {
        initRoles();

        // create applicationTypeSchemaDto1
        ApplicationTypeSchemaDto applicationTypeSchemaDto1 = createAppTypeSchema("1");
        applicationTypeSchemaFacade.create(applicationTypeSchemaDto1);
        // create application1
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(URI.create(applicationTypeSchemaDto1.getId()));
        applicationFacade.createApplication(applicationDto);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = applicationFacade.getAllApplications();
        var actualApplicationAtRevision = applicationFacade.getApplication(applicationDto.getName());

        // create applicationTypeSchemaDto1
        ApplicationTypeSchemaDto applicationTypeSchemaDto2 = createAppTypeSchema("2");
        applicationTypeSchemaFacade.create(applicationTypeSchemaDto2);

        // update application
        applicationDto.setCustomAppSchemaId(URI.create(applicationTypeSchemaDto2.getId()));
        applicationFacade.updateApplication(applicationDto.getName(), applicationDto, "*");

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ApplicationInfoDto> applicationsAfterRollbackToRevision = applicationFacade.getAllApplications();
        var applicationAfterRollbackToRevision = applicationFacade.getApplication(applicationDto.getName());
        Assertions.assertEquals(actualAtRevision, applicationsAfterRollbackToRevision);
        Assertions.assertEquals(actualApplicationAtRevision, applicationAfterRollbackToRevision);
    }

    @Test
    public void shouldSuccessfullyRollbackApplicationsWithAppTypeSchemasWhenReassignSchemaToOtherApplication() {
        initRoles();

        // create applicationTypeSchemaDto
        ApplicationTypeSchemaDto applicationTypeSchemaDto = createAppTypeSchema("1");
        applicationTypeSchemaFacade.create(applicationTypeSchemaDto);

        // create application1
        ApplicationDto application1Dto = createBaseApplicationDto("1");
        application1Dto.setCustomAppSchemaId(URI.create(applicationTypeSchemaDto.getId()));
        applicationFacade.createApplication(application1Dto);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        var actualAtRevision = applicationFacade.getAllApplications();
        var actualApplication1AtRevision = applicationFacade.getApplication(application1Dto.getName());

        application1Dto.setCustomAppSchemaId(null);
        application1Dto.setEndpoint("endpoint");
        applicationFacade.updateApplication(application1Dto.getName(), application1Dto, "*");

        ApplicationDto application2Dto = createBaseApplicationDto("2");
        application2Dto.setCustomAppSchemaId(URI.create(applicationTypeSchemaDto.getId()));
        applicationFacade.createApplication(application2Dto);

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ApplicationInfoDto> applicationsAfterRollbackToRevision = applicationFacade.getAllApplications();
        var applicationAfterRollbackToRevision = applicationFacade.getApplication(application1Dto.getName());
        Assertions.assertEquals(actualAtRevision, applicationsAfterRollbackToRevision);
        Assertions.assertEquals(actualApplication1AtRevision, applicationAfterRollbackToRevision);
    }

    private ApplicationTypeSchemaDto createAppTypeSchema(String suffix) {
        ApplicationTypeSchemaDto dto = new ApplicationTypeSchemaDto();
        dto.setId("https://test-schema.example/" + suffix);
        dto.setApplicationTypeDisplayName(dto.getId());
        return dto;
    }

    private void assertApplication(ApplicationDto actual, ApplicationDto expected) {
        Assertions.assertEquals(expected, actual);
    }
}
