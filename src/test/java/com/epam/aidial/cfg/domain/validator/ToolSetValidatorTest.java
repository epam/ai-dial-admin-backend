package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.ToolSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ToolSetValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";
    private static final String TEST_TOOLSET_NAME = "test-toolset";

    @Mock
    private DeploymentValidator deploymentValidator;
    
    private ToolSetValidator toolSetValidator;
    
    @BeforeEach
    void setUp() {
        toolSetValidator = new ToolSetValidator(deploymentValidator, null);
    }
    
    @Test
    void validateCreation_shouldDelegateToDeploymentValidator() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        Deployment deployment = new Deployment(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);
        
        // when & then
        assertThatNoException().isThrownBy(() -> toolSetValidator.validateCreation(toolSet));
        verify(deploymentValidator).validateCreation("ToolSet", toolSet.getDisplayName());
    }
    
    @Test
    void validateCreation_withNameValidationPattern_shouldValidateNameAgainstPattern() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        Deployment deployment = new Deployment(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);
        ReflectionTestUtils.setField(toolSetValidator, "toolSetNameValidationPattern", NAME_VALIDATION_PATTERN);
        
        // when & then
        assertThatNoException().isThrownBy(() -> toolSetValidator.validateCreation(toolSet));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"invalid name", "invalid@name", "invalid#name"})
    void validateCreation_withInvalidNameAgainstPattern_shouldThrowException(String invalidName) {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(invalidName);
        Deployment deployment = new Deployment(invalidName);
        toolSet.setDeployment(deployment);
        ReflectionTestUtils.setField(toolSetValidator, "toolSetNameValidationPattern", NAME_VALIDATION_PATTERN);
        
        // when & then
        assertThatThrownBy(() -> toolSetValidator.validateCreation(toolSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern: " + NAME_VALIDATION_PATTERN);
    }
    
    @Test
    void validateCreation_withoutNameValidationPattern_shouldNotValidateNameAgainstPattern() {
        // given
        String toolSetName = "invalid name with spaces";
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(toolSetName);
        Deployment deployment = new Deployment(toolSetName);
        toolSet.setDeployment(deployment);
        
        // when & then
        assertThatNoException().isThrownBy(() -> toolSetValidator.validateCreation(toolSet));
    }
    
    @Test
    void validateUpdate_shouldDelegateToDeploymentValidator() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        Deployment deployment = new Deployment(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);
        
        // when & then
        assertThatNoException().isThrownBy(() -> toolSetValidator.validateUpdate(TEST_TOOLSET_NAME, toolSet));
        verify(deploymentValidator).validateUpdate(TEST_TOOLSET_NAME, toolSet.getDeployment(), "ToolSet");
    }
    
    @Test
    void validateToolSetFields_withNullEndpoint_shouldNotThrowException() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        toolSet.setEndpoint(null);
        Deployment deployment = new Deployment(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);
        
        // when & then
        assertThatNoException().isThrownBy(() -> ReflectionTestUtils.invokeMethod(toolSetValidator, "validateToolSetFields", toolSet));
    }
    
    @Test
    void validateToolSetFields_withValidEndpoint_shouldNotThrowException() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        toolSet.setEndpoint("https://example.com/api");
        Deployment deployment = new Deployment(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);
        
        // when & then
        assertThatNoException().isThrownBy(() -> ReflectionTestUtils.invokeMethod(toolSetValidator, "validateToolSetFields", toolSet));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void validateToolSetFields_withBlankEndpoint_shouldThrowException(String blankEndpoint) {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        toolSet.setEndpoint(blankEndpoint);
        Deployment deployment = new Deployment(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);
        
        // when & then
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(toolSetValidator, "validateToolSetFields", toolSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid endpoint");
    }
}