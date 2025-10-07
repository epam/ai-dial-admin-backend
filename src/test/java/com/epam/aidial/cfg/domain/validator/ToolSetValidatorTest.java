package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetEndpointsSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ToolSetValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";
    private static final String TEST_TOOLSET_NAME = "test-toolset";
    private static final String TEST_CONTAINER_NAME = "test-container";
    private static final String TEST_CONTAINER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String COMPLETION_PATH = "/api/completion";

    @Mock
    private DeploymentValidator deploymentValidator;
    @Mock
    private DeploymentManagerService deploymentManagerService;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;

    private ToolSetValidator toolSetValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        toolSetValidator = new ToolSetValidator(
                deploymentManagerService,
                new DeploymentInfoValidator(),
                deploymentValidator,
                displayFieldsValidator,
                null
        );
    }

    @Test
    void validateCreation_shouldDelegateToDeploymentValidator() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        SecuredResource deployment = new SecuredResource(TEST_TOOLSET_NAME);
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
        SecuredResource deployment = new SecuredResource(TEST_TOOLSET_NAME);
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
        SecuredResource deployment = new SecuredResource(invalidName);
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
        SecuredResource deployment = new SecuredResource(toolSetName);
        toolSet.setDeployment(deployment);

        // when & then
        assertThatNoException().isThrownBy(() -> toolSetValidator.validateCreation(toolSet));
    }

    @Test
    void validateUpdate_shouldDelegateToDeploymentValidator() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        SecuredResource deployment = new SecuredResource(TEST_TOOLSET_NAME);
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
        SecuredResource deployment = new SecuredResource(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);

        // when & then
        assertThatNoException().isThrownBy(() -> ReflectionTestUtils.invokeMethod(toolSetValidator, "validateToolSetSource", toolSet));
    }

    @Test
    void validateToolSetFields_withValidEndpoint_shouldNotThrowException() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        toolSet.setEndpoint("https://example.com/api");
        SecuredResource deployment = new SecuredResource(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);

        // when & then
        assertThatNoException().isThrownBy(() -> ReflectionTestUtils.invokeMethod(toolSetValidator, "validateToolSetSource", toolSet));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void validateToolSetFields_withBlankEndpoint_shouldThrowException(String blankEndpoint) {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(TEST_TOOLSET_NAME);
        toolSet.setEndpoint(blankEndpoint);
        SecuredResource deployment = new SecuredResource(TEST_TOOLSET_NAME);
        toolSet.setDeployment(deployment);

        // when & then
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(toolSetValidator, "validateToolSetSource", toolSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid endpoint");
    }

    @Test
    void validateEndpointsSource_shouldThrowExceptionWhenCompletionEndpointIsMissing() {
        // given
        SecuredResource deployment = new SecuredResource("test-toolset");
        ToolSet toolSet = new ToolSet();
        toolSet.setDeployment(deployment);
        toolSet.setEndpoint(null);
        toolSet.setSource(new ToolSetEndpointsSource());

        // when/then
        assertThatThrownBy(() -> toolSetValidator.validateCreation(toolSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Endpoint is required when source type is 'Toolset endpoints'. Toolset: test-toolset");
    }

    @Test
    void validateEndpointsSource_shouldValidateEndpoints() {
        // given
        SecuredResource deployment = new SecuredResource("test-toolset");
        ToolSet toolSet = new ToolSet();
        toolSet.setDeployment(deployment);
        toolSet.setEndpoint("invalid-url");
        toolSet.setSource(new ToolSetEndpointsSource());

        // when/then
        assertThatThrownBy(() -> toolSetValidator.validateCreation(toolSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint: 'invalid-url'. Toolset: test-toolset");
    }

    @Test
    void validateContainerSource_shouldThrowExceptionWhenContainerNotFound() {
        // given
        SecuredResource deployment = new SecuredResource("test-toolset");
        ToolSet toolSet = new ToolSet();
        toolSet.setDeployment(deployment);
        toolSet.setSource(new ToolSetContainerSource(TEST_CONTAINER_ID, TEST_CONTAINER_NAME, COMPLETION_PATH));

        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> toolSetValidator.validateCreation(toolSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container with ID '550e8400-e29b-41d4-a716-446655440000' not found");
    }

    @Test
    void validateContainerSource_shouldThrowExceptionWhenDeploymentUrlIsBlank() {
        // given
        SecuredResource deployment = new SecuredResource("test-toolset");
        ToolSet toolSet = new ToolSet();
        toolSet.setDeployment(deployment);
        toolSet.setSource(new ToolSetContainerSource(TEST_CONTAINER_ID, TEST_CONTAINER_NAME, COMPLETION_PATH));

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl("");
        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatThrownBy(() -> toolSetValidator.validateCreation(toolSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container URL is not present, please check if it is deployed. Container ID: 550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void validateContainerSource_shouldValidateEndpointPaths() {
        // given
        SecuredResource deployment = new SecuredResource("test-toolset");
        ToolSet toolSet = new ToolSet();
        toolSet.setDeployment(deployment);
        toolSet.setSource(new ToolSetContainerSource(TEST_CONTAINER_ID, TEST_CONTAINER_NAME, "invalid path with spaces"));

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl("https://deployment.url");
        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatThrownBy(() -> toolSetValidator.validateCreation(toolSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint path: 'invalid path with spaces'. Toolset: test-toolset");
    }

    @Test
    void validateContainerSource_shouldAcceptValidEndpointPaths() {
        // given
        SecuredResource deployment = new SecuredResource("test-toolset");
        ToolSet toolSet = new ToolSet();
        toolSet.setDeployment(deployment);
        toolSet.setSource(new ToolSetContainerSource(TEST_CONTAINER_ID, TEST_CONTAINER_NAME, COMPLETION_PATH));

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl("https://deployment.url");
        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatNoException().isThrownBy(() -> toolSetValidator.validateCreation(toolSet));
    }
}