package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AssistantsPropertyDto;
import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.web.facade.AssistantsPropertyFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public abstract class AssistantsPropertyFunctionalTest {

    @Autowired
    private AssistantsPropertyFacade assistantsPropertyFacade;
    @Autowired
    private FeatureFlagGateEvaluationAspect featureFlagAspect;

    @Test
    public void shouldSuccessfullyCreateAndGetAssistants() {
        AssistantsPropertyDto assistantsPropertyDto = new AssistantsPropertyDto();
        AssistantsPropertyDto actual1 = assistantsPropertyFacade.getAssistantsProperty();
        Assertions.assertEquals(assistantsPropertyDto, actual1);

        assistantsPropertyDto.setEndpoint("test-endpoint");
        assistantsPropertyDto.getFeatures().setAllowResume(true);
        assistantsPropertyDto.getFeatures().setConfigurationEndpoint("test config endpoint");
        assistantsPropertyFacade.updateAssistantsProperty(assistantsPropertyDto);
        AssistantsPropertyDto actual2 = assistantsPropertyFacade.getAssistantsProperty();
        Assertions.assertEquals(assistantsPropertyDto, actual2);
    }

    @Test
    public void shouldSuccessfullyUpdateAssistantsWhenDbIsEmpty() {
        AssistantsPropertyDto assistantsPropertyDto = new AssistantsPropertyDto();
        assistantsPropertyDto.setEndpoint("test-endpoint2");
        assistantsPropertyDto.getFeatures().setAllowResume(true);
        assistantsPropertyDto.getFeatures().setConfigurationEndpoint("test config endpoint2");
        assistantsPropertyFacade.updateAssistantsProperty(assistantsPropertyDto);
        AssistantsPropertyDto actual = assistantsPropertyFacade.getAssistantsProperty();
        Assertions.assertEquals(assistantsPropertyDto, actual);
    }

    @Test
    void testUpdate_UnsupportedException() {
        // given
        AssistantsPropertyDto assistantsPropertyDto = new AssistantsPropertyDto();
        doThrow(new UnsupportedOperationException("Feature flag 'assistantsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> assistantsPropertyFacade.updateAssistantsProperty(assistantsPropertyDto))
                // then
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'assistantsSupported' is disabled.");
    }
}
