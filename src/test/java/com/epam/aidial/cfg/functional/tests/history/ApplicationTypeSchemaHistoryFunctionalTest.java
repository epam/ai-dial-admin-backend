package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.functional.utils.FunctionalTestHelper;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createBaseApplicationDto;

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
        applicationTypeSchemaFacade.get("https://test-schema.example/1");

        // 2 update application1 description
        ApplicationTypeSchemaDto updatedApplicationTypeSchema = createDto("1");
        updatedApplicationTypeSchema.setInterceptors(List.of("interceptor1"));
        updatedApplicationTypeSchema.setDescription("new application description");
        applicationTypeSchemaFacade.update(applicationDto.getId(), updatedApplicationTypeSchema, "*");

        // verify application1
        ApplicationTypeSchemaDto actual = applicationTypeSchemaFacade.get(applicationDto.getId());
        var expected = createDto("1");
        expected.setDescription("new application description");
        expected.setApplications(List.of());
        expected.setAppendApplicationPropertiesHeader(true);
        expected.setInterceptors(List.of("interceptor1"));
        expected.setApplicationTypeAssistantAttachmentsInRequestSupported(false);
        expected.setApplicationTypeSchemaEndpoint("https://test.com/endpoint_1");
        expected.setApplicationTypeResponsesEndpoint("https://test.com/responses/endpoint_1");
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

    @ParameterizedTest
    @CsvSource({"true", "false"})
    public void shouldSuccessfullyRollbackDeletedApplicationTypeSchemaWithApplication(boolean removeApplication) {
        // create application type schema
        ApplicationTypeSchemaDto applicationTypeSchemaDto = createDto("1");
        applicationTypeSchemaFacade.create(applicationTypeSchemaDto);

        // create application
        ApplicationDto applicationDto = createBaseApplicationDto("1");
        applicationDto.setCustomAppSchemaId(URI.create(applicationTypeSchemaDto.getId()));
        applicationFacade.createApplication(applicationDto);

        // remember rev number and expected application type schemas state
        Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();
        Collection<ApplicationTypeSchemaDto> actualAtRevision = applicationTypeSchemaFacade.getAll();

        // delete application type schema
        applicationTypeSchemaFacade.delete(applicationTypeSchemaDto.getId(), removeApplication);

        // rollback and verify
        int revisionsListSizeBeforeRollback = historyFacade.getRevisionsListSize();
        historyFacade.rollbackToRevision(revNumberToRollback);
        int revisionsListSizeAfterRollback = historyFacade.getRevisionsListSize();

        Assertions.assertEquals(revisionsListSizeBeforeRollback + 1, revisionsListSizeAfterRollback);

        Collection<ApplicationTypeSchemaDto> applicationTypeSchemasAfterRollbackToRevision = applicationTypeSchemaFacade.getAll();
        Assertions.assertEquals(actualAtRevision, applicationTypeSchemasAfterRollbackToRevision);
    }

    private void assertApplicationTypeSchema(ApplicationTypeSchemaDto actual, ApplicationTypeSchemaDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private ApplicationTypeSchemaDto createDto(String suffix) {
        ApplicationTypeSchemaDto applicationDto = new ApplicationTypeSchemaDto();
        applicationDto.setId("https://test-schema.example/" + suffix);
        applicationDto.setDescription("description" + suffix);
        applicationDto.setApplicationTypeDisplayName("id" + suffix);
        applicationDto.setApplicationTypeSchemaEndpoint("https://test.com/endpoint_" + suffix);
        applicationDto.setApplicationTypeResponsesEndpoint("https://test.com/responses/endpoint_" + suffix);
        return applicationDto;
    }
}