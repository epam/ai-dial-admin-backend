package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.route.Route;
import org.assertj.core.api.Assertions;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RouteValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    @Mock
    private DeploymentValidator deploymentValidator;

    private RouteValidator routeValidator;

    @BeforeEach
    void setUp() {
        routeValidator = new RouteValidator(deploymentValidator, null);
    }

    @Test
    void validateUpdate_shouldDelegateToDeploymentValidator() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Route route = new Route();
        route.setDeployment(deployment);

        // when
        routeValidator.validateUpdate(deploymentName, route);

        // then
        verify(deploymentValidator).validateUpdate(deploymentName, deployment, "Route");
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateRouteCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(routeValidator, "routeNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Route route = new Route();
        route.setDeployment(deployment);

        // when/then
        assertThatNoException().isThrownBy(() -> routeValidator.validateRouteCreation(route));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name",
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateRouteCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(routeValidator, "routeNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Route route = new Route();
        route.setDeployment(deployment);

        // when/then
        assertThatThrownBy(() -> routeValidator.validateRouteCreation(route))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateRouteCreation_shouldThrowExceptionWhenDeploymentValidatorThrows() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Route route = new Route();
        route.setDeployment(deployment);

        doThrow(IllegalArgumentException.class).when(deploymentValidator)
                .validateCreation("Route", deploymentName);

        // when/then
        Assertions.assertThatThrownBy(() -> routeValidator.validateRouteCreation(route))
                .isInstanceOf(IllegalArgumentException.class);
    }

}