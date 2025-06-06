package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
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
    public void shouldSuccessfullyCreateAndGetAssistants() {
        initRoles();
        AssistantDto assistantDto = createDto("1");

        assistantFacade.createAssistant(assistantDto);

        AssistantDto actual = assistantFacade.getAssistant(assistantDto.getName());
        AssistantDto expected = createDto("1");

        assertAssistant(actual, expected);

        assistantFacade.createAssistant(createDto("2"));

        Collection<AssistantDto> actualAssistants = assistantFacade.getAllAssistants();

        assertAssistants(actualAssistants, List.of(createDto("1"), createDto("2")));
    }

    @Test
    void testCreate_UnsupportedException() {
        // given
        AssistantDto assistantDto = createDto("1");
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
        AssistantDto assistantDto = createDto("1");
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
        AssistantDto assistantDto = createDto("1");
        assistantFacade.createAssistant(assistantDto);

        assistantFacade.deleteAssistant(assistantDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> assistantFacade.getAssistant(assistantDto.getName()));
        Assertions.assertTrue(assistantFacade.getAllAssistants().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateAssistant() {
        initRoles();
        AssistantDto assistantDto = createDto("1");
        assistantFacade.createAssistant(assistantDto);
        AssistantDto updatedAssistant = createDto("1");
        updatedAssistant.setDescription("new assistant description");

        assistantFacade.updateAssistant(assistantDto.getName(), updatedAssistant);

        AssistantDto actual = assistantFacade.getAssistant(assistantDto.getName());
        var expected = createDto("1");
        expected.setDescription("new assistant description");
        assertAssistant(actual, expected);
    }

    @Test
    public void shouldThrowExceptionWhenRenameAssistant() {
        initRoles();
        AssistantDto assistantDto = createDto("1");
        assistantFacade.createAssistant(assistantDto);
        AssistantDto updatedAssistant = createDto("2");
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

    private AssistantDto createDto(String suffix) {
        AssistantDto assistantDto = new AssistantDto();
        assistantDto.setName("assistant" + suffix);
        assistantDto.setDescription("description" + suffix);
        assistantDto.setRoleLimits(Map.of(
                "role2", new LimitDto()
        ));
        return assistantDto;
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
