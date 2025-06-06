package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AssistantsPropertyDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.FeaturesDto;
import com.epam.aidial.cfg.web.facade.AssistantsPropertyFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

public abstract class AssistantsPropertyHistoryFunctionalTest {

    @Autowired
    private AssistantsPropertyFacade assistantsPropertyFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    @Test
    public void shouldSuccessfullyCreateAndUpdateAssistant() {

        AssistantsPropertyDto assistantsProperty = new AssistantsPropertyDto();
        assistantsProperty.setEndpoint("endpoint1");
        assistantsProperty.setFeatures(new FeaturesDto());
        assistantsPropertyFacade.updateAssistantsProperty(assistantsProperty);

        assistantsProperty.setEndpoint("endpoint2");
        assistantsPropertyFacade.updateAssistantsProperty(assistantsProperty);
        var actualAtRevision = assistantsPropertyFacade.getAssistantsProperty();
        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        assistantsProperty.setEndpoint("endpoint3");
        assistantsPropertyFacade.updateAssistantsProperty(assistantsProperty);

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        var actual = assistantsPropertyFacade.getAssistantsProperty();
        Assertions.assertEquals(actual, actualAtRevision);
    }
}
