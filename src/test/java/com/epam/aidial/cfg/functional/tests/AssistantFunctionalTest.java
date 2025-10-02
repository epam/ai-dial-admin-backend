package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.web.facade.AssistantFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createAssistantDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public abstract class AssistantFunctionalTest {

    @Autowired
    private AssistantFacade assistantFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private FeatureFlagGateEvaluationAspect featureFlagAspect;

    private void initRoles() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetAssistants() {
        initRoles();
        AssistantDto assistantDto = createAssistantDto("1");

        assistantFacade.createAssistant(assistantDto);

        AssistantDto actual = assistantFacade.getAssistant(assistantDto.getName());
        AssistantDto expected = createAssistantDto("1");

        assertAssistant(actual, expected);

        assistantFacade.createAssistant(createAssistantDto("2"));

        Collection<AssistantDto> actualAssistants = assistantFacade.getAllAssistants();

        assertAssistants(actualAssistants, List.of(createAssistantDto("1"), createAssistantDto("2")));
    }

    @Test
    void testCreate_UnsupportedException() {
        // given
        AssistantDto assistantDto = createAssistantDto("1");
        doThrow(new UnsupportedOperationException("Feature flag 'assistantsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> assistantFacade.createAssistant(assistantDto))
                // then
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'assistantsSupported' is disabled.");
    }

    @Test
    void testUpdate_UnsupportedException() {
        // given
        AssistantDto assistantDto = createAssistantDto("1");
        doThrow(new UnsupportedOperationException("Feature flag 'assistantsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> assistantFacade.updateAssistant(assistantDto.getName(), assistantDto))
                // then
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'assistantsSupported' is disabled.");
    }

    @Test
    void testDelete_UnsupportedException() {
        // given
        doThrow(new UnsupportedOperationException("Feature flag 'assistantsSupported' is disabled.")).when(featureFlagAspect).evaluate(any(), any());
        // when
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> assistantFacade.deleteAssistant("assistantName"))
                // then
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Feature flag 'assistantsSupported' is disabled.");
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteAssistant() {
        initRoles();
        AssistantDto assistantDto = createAssistantDto("1");
        assistantFacade.createAssistant(assistantDto);

        assistantFacade.deleteAssistant(assistantDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> assistantFacade.getAssistant(assistantDto.getName()));
        Assertions.assertTrue(assistantFacade.getAllAssistants().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateAssistant() {
        initRoles();
        AssistantDto assistantDto = createAssistantDto("1");
        assistantFacade.createAssistant(assistantDto);
        AssistantDto updatedAssistant = createAssistantDto("1");
        updatedAssistant.setDescription("new assistant description");

        assistantFacade.updateAssistant(assistantDto.getName(), updatedAssistant);

        AssistantDto actual = assistantFacade.getAssistant(assistantDto.getName());
        var expected = createAssistantDto("1");
        expected.setDescription("new assistant description");
        assertAssistant(actual, expected);
    }

    @Test
    public void shouldThrowExceptionWhenRenameAssistant() {
        initRoles();
        AssistantDto assistantDto = createAssistantDto("1");
        assistantFacade.createAssistant(assistantDto);
        AssistantDto updatedAssistant = createAssistantDto("2");
        updatedAssistant.setDescription("new assistant description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> assistantFacade.updateAssistant(assistantDto.getName(), updatedAssistant)
        );
        Assertions.assertEquals("Assistant with name: 'assistant1' can not be renamed. New name: 'assistant2'", exception.getMessage());
    }

    private void assertAssistant(AssistantDto actual, AssistantDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        Assertions.assertEquals(expected.getRoleLimits(), actual.getRoleLimits());
    }

    private Map<String, AssistantDto> toMap(Collection<AssistantDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(AssistantDto::getName, Function.identity()));
    }

    private void assertAssistants(Collection<AssistantDto> actual, Collection<AssistantDto> expected) {
        Map<String, AssistantDto> actualMap = toMap(actual);
        Map<String, AssistantDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertAssistant(actualMap.get(name), expectedMap.get(name));
        }
    }

}
