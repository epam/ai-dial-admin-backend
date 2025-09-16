package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.exception.NotModifiedException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        var status = response.status();
        switch (status) {
            case 304 -> {
                return new NotModifiedException(response.headers());
            }
            case 404 -> {
                return new ResourceNotFoundException(response.reason());
            }
            case 412 -> {
                return new ResourcePreconditionFailedException(response.reason());
            }
            default -> {
                return FeignException.errorStatus(methodKey, response);
            }
        }
    }
}
