package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Interceptor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InterceptorValidator {

    public void validateUpdate(String interceptorName, Interceptor interceptor) {
        if (!Objects.equals(interceptorName, interceptor.getName())) {
            throw new IllegalArgumentException("Interceptor with name: '" + interceptorName + "' can not be renamed. New interceptor name: '" + interceptor.getName() + "'");
        }
    }
}
