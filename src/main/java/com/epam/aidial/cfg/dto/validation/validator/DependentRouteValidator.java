package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.ResponseDto;
import com.epam.aidial.cfg.dto.UpstreamDto;
import com.epam.aidial.cfg.dto.route.DependentRouteDto;
import com.epam.aidial.cfg.dto.validation.annotation.DependentRoute;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class DependentRouteValidator implements ConstraintValidator<DependentRoute, DependentRouteDto> {

    @Override
    public boolean isValid(DependentRouteDto route, ConstraintValidatorContext context) {
        if (route == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        return validateRequiredFields(route, context)
                && validateResponseFields(route, context)
                && validateUpstreamEndpoints(route, context);
    }

    private boolean validateRequiredFields(DependentRouteDto route, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (route.getMethods() == null) {
            context.buildConstraintViolationWithTemplate("Methods must be provided")
                    .addPropertyNode("applicationTypeRoutes")
                    .addPropertyNode(route.getName())
                    .addPropertyNode("methods")
                    .addConstraintViolation();
            isValid = false;
        }

        if (route.getUpstreams() == null) {
            context.buildConstraintViolationWithTemplate("Upstreams must be provided")
                    .addPropertyNode("applicationTypeRoutes")
                    .addPropertyNode(route.getName())
                    .addPropertyNode("upstreams")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }

    private boolean validateResponseFields(DependentRouteDto route, ConstraintValidatorContext context) {
        ResponseDto response = route.getResponse();
        if (response == null) {
            return true;
        }

        boolean isValid = true;

        if (response.getStatus() <= 0) {
            context.buildConstraintViolationWithTemplate("Response status must be provided")
                    .addPropertyNode("applicationTypeRoutes")
                    .addPropertyNode(route.getName())
                    .addPropertyNode("response")
                    .addPropertyNode("status")
                    .addConstraintViolation();
            isValid = false;
        }

        if (response.getBody() == null) {
            context.buildConstraintViolationWithTemplate("Response body must be provided")
                    .addPropertyNode("applicationTypeRoutes")
                    .addPropertyNode(route.getName())
                    .addPropertyNode("response")
                    .addPropertyNode("body")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }

    private boolean validateUpstreamEndpoints(DependentRouteDto route, ConstraintValidatorContext context) {
        List<UpstreamDto> upstreams = route.getUpstreams();
        if (CollectionUtils.isEmpty(upstreams)) {
            return true;
        }

        boolean isValid = true;

        for (int i = 0; i < upstreams.size(); i++) {
            UpstreamDto upstream = upstreams.get(i);
            if (upstream != null && StringUtils.isEmpty(upstream.getEndpoint())) {
                context.buildConstraintViolationWithTemplate("Upstream endpoint must be provided")
                        .addPropertyNode("applicationTypeRoutes")
                        .addPropertyNode(route.getName())
                        .addPropertyNode("upstreams")
                        .addPropertyNode("endpoint")
                        .inIterable().atIndex(i)
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}