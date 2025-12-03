package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.ResponseDto;
import com.epam.aidial.cfg.dto.UpstreamDto;
import com.epam.aidial.cfg.dto.route.DependentRouteDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependentRouteValidatorTest {

    private Validator validator;
    private DependentRouteDto route;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        route = new DependentRouteDto();
        route.setName("someRoute");
        route.setPaths(List.of("/api/test"));
        route.setMethods(new HashSet<>(Arrays.asList("GET", "POST")));

        UpstreamDto upstream = new UpstreamDto();
        upstream.setEndpoint("http://example.com/api");
        route.setUpstreams(List.of(upstream));

        ResponseDto response = new ResponseDto();
        response.setStatus(200);
        response.setBody("Success");
        route.setResponse(response);
    }

    @Test
    void testValidRoute() {
        Set<ConstraintViolation<DependentRouteDto>> violations = validator.validate(route);
        assertTrue(violations.isEmpty(), "A valid route should not have validation violations");
    }

    @Test
    void testNullMethods() {
        route.setMethods(null);
        Set<ConstraintViolation<DependentRouteDto>> violations = validator.validate(route);
        assertFalse(violations.isEmpty(), "Null methods should cause validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Methods must be provided")));
    }

    @Test
    void testNullUpstreams() {
        route.setUpstreams(null);
        Set<ConstraintViolation<DependentRouteDto>> violations = validator.validate(route);
        assertFalse(violations.isEmpty(), "Null upstreams should cause validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Upstreams must be provided")));
    }

    @Test
    void testInvalidResponse() {
        ResponseDto response = new ResponseDto();
        response.setStatus(0);
        response.setBody(null);
        route.setResponse(response);

        Set<ConstraintViolation<DependentRouteDto>> violations = validator.validate(route);
        assertFalse(violations.isEmpty(), "Invalid response should cause validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Response status must be provided")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Response body must be provided")));
    }

    @Test
    void testNullUpstreamEndpoint() {
        UpstreamDto upstream = new UpstreamDto();
        upstream.setEndpoint(null);
        route.setUpstreams(List.of(upstream));

        Set<ConstraintViolation<DependentRouteDto>> violations = validator.validate(route);
        assertFalse(violations.isEmpty(), "Null upstream endpoint should cause validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Upstream endpoint must be provided")));
    }

    @Test
    void testInvalidUpstreamEndpoint() {
        UpstreamDto upstream = new UpstreamDto();
        upstream.setEndpoint("  ");
        route.setUpstreams(List.of(upstream));

        Set<ConstraintViolation<DependentRouteDto>> violations = validator.validate(route);
        assertFalse(violations.isEmpty(), "Empty upstream endpoint should cause validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Invalid upstream endpoint")));
    }

    @Test
    void testEmptyUpstreams() {
        route.setUpstreams(Collections.emptyList());
        Set<ConstraintViolation<DependentRouteDto>> violations = validator.validate(route);
        assertTrue(violations.isEmpty(), "Empty upstreams list should be valid");
    }

    @Test
    void testNullResponse() {
        route.setResponse(null);
        Set<ConstraintViolation<DependentRouteDto>> violations = validator.validate(route);
        assertTrue(violations.isEmpty(), "Null response should be valid");
    }
}