package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.AuthenticationTypeDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.ResourceAuthSettingsDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collection;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAuditActivityDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createModelDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createToolSetDtoWithoutRoleLimits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public abstract class AssociationsOneToOneHistoryFunctionalTests {

    @Autowired
    private ModelFacade modelFacade;
    @Autowired
    private ToolSetFacade toolSetFacade;
    @Autowired
    private TestHistoryFacade historyFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenModelDeploymentFieldIsUpdated() {
        // create model
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        ModelDto modelDto = createModelDto("1");
        modelFacade.createModel(modelDto);

        // update model default role limit
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);

        modelDto = createModelDto("1");
        modelDto.setDefaultRoleLimit(limitDto);
        modelFacade.updateModel(modelDto.getName(), modelDto, "*");

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(modelFacade.getModel(modelDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(222L));

        AuditActivityDto expectedModelActivity = createAuditActivityDto("Update", "Model", modelDto.getName(), 222L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedModelActivity);
    }

    @Test
    public void shouldCorrectlyTrackUpdatedAtAndAuditActivitiesWhenToolSetSecuredResourceFieldIsUpdated() {
        // create toolSet
        doReturn(111L).when(transactionTimestampContext).getTimestamp();
        ToolSetDto toolSetDto = createToolSetDtoWithoutRoleLimits("1");
        toolSetFacade.createToolSet(toolSetDto);

        // update toolSet resource auth settings
        doReturn(222L).when(transactionTimestampContext).getTimestamp();
        ResourceAuthSettingsDto resourceAuthSettingsDto = new ResourceAuthSettingsDto();
        resourceAuthSettingsDto.setAuthenticationType(AuthenticationTypeDto.OAUTH);
        resourceAuthSettingsDto.setClientId("clientId");
        resourceAuthSettingsDto.setClientSecret("clientSecret");
        resourceAuthSettingsDto.setAuthorizationEndpoint("/authorize");
        resourceAuthSettingsDto.setTokenEndpoint("/token");
        resourceAuthSettingsDto.setCodeChallengeMethod("S256");

        toolSetDto = createToolSetDtoWithoutRoleLimits("1");
        toolSetDto.setAuthSettings(resourceAuthSettingsDto);
        toolSetFacade.updateToolSet(toolSetDto.getName(), toolSetDto, "*");

        Integer latestRevision = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // verify
        Assertions.assertThat(toolSetFacade.getToolSet(toolSetDto.getName()).getUpdatedAt()).isEqualTo(Instant.ofEpochMilli(222L));

        AuditActivityDto expectedToolSetActivity = createAuditActivityDto("Update", "ToolSet", toolSetDto.getName(), 222L);

        Collection<AuditActivityDto> auditActivities = historyFacade.getActivitiesAtRevision(latestRevision).getData();
        assertThat(auditActivities)
                .usingRecursiveFieldByFieldElementComparatorOnFields("activityType", "resourceType", "resourceId", "epochTimestampMs")
                .containsExactlyInAnyOrder(expectedToolSetActivity);
    }
}
