package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.functional.utils.FunctionalTestHelper;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class ApplicationTypeSchemaHistoryFunctionalTest {

    @Autowired
    private ApplicationTypeSchemaFacade applicationTypeSchemaFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private ApplicationFacade applicationFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldSuccessfullyCreateAndUpdateApplicationTypeSchema() {
        // 1 create application1
        interceptorFacade.createInterceptor(FunctionalTestHelper.createInterceptorDto("1"));
        interceptorFacade.createInterceptor(FunctionalTestHelper.createInterceptorDto("2"));
        ApplicationTypeSchemaDto applicationDto = createDto("1");
        applicationDto.setInterceptors(List.of("interceptor1", "interceptor2"));
        applicationTypeSchemaFacade.create(applicationDto);
        applicationTypeSchemaFacade.get("id1");

        // 2 update application1 description
        ApplicationTypeSchemaDto updatedApplicationTypeSchema = createDto("1");
        updatedApplicationTypeSchema.setInterceptors(List.of("interceptor1"));
        updatedApplicationTypeSchema.setDescription("new application description");
        applicationTypeSchemaFacade.update(applicationDto.getId(), updatedApplicationTypeSchema, "*");

        // verify application1
        ApplicationTypeSchemaDto actual = applicationTypeSchemaFacade.get(applicationDto.getId());
        var expected = createDto("1");
        expected.setDescription("new application description");
        expected.setDefs(Map.of());
        expected.setProperties(Map.of());
        expected.setApplications(List.of());
        expected.setApplicationTypeRoutes(List.of());
        expected.setAppendApplicationPropertiesHeader(true);
        expected.setInterceptors(List.of("interceptor1"));
        expected.setApplicationTypeAssistantAttachmentsInRequestSupported(false);
        assertApplicationTypeSchema(actual, expected);

        var actualAtOldRevision = applicationTypeSchemaFacade.getAll();
        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // 3 update application type schema
        updatedApplicationTypeSchema = createDto("1");
        updatedApplicationTypeSchema.setDescription("new new application description");
        applicationTypeSchemaFacade.update(applicationDto.getId(), updatedApplicationTypeSchema, "*");

        // 6 delete application 1
        applicationTypeSchemaFacade.delete(applicationDto.getId(), false);

        // 7 create application 2
        applicationTypeSchemaFacade.create(createDto("2"));

        // 9 create application3 with assigned role3
        applicationTypeSchemaFacade.create(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<ApplicationTypeSchemaDto> applicationsAfterRollback = applicationTypeSchemaFacade.getAll();
        Assertions.assertEquals(actualAtOldRevision, applicationsAfterRollback);
    }

    private ApplicationTypeSchemaDto createAppTypeSchema(String suffix) {
        ApplicationTypeSchemaDto dto = new ApplicationTypeSchemaDto();
        dto.setId("https://test-schema.example/" + suffix);
        return dto;
    }

    private void assertApplicationTypeSchema(ApplicationTypeSchemaDto actual, ApplicationTypeSchemaDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private ApplicationTypeSchemaDto createDto(String suffix) {
        ApplicationTypeSchemaDto applicationDto = new ApplicationTypeSchemaDto();
        applicationDto.setId("id" + suffix);
        applicationDto.setDescription("description" + suffix);
        applicationDto.setApplicationTypeDisplayName("id" + suffix);
        return applicationDto;
    }
}