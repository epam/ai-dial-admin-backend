package com.epam.aidial.cfg.domain.validator;

import lombok.experimental.UtilityClass;
import org.apache.commons.validator.routines.UrlValidator;

import static org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS;

@UtilityClass
public class EndpointValidator {

    public static boolean isInvalidUrl(String url) {
        return !isValidUrl(url);
    }

    public static boolean isValidUrl(String url) {
        String[] schemes = {"http", "https"};
        var validator = new UrlValidator(schemes, ALLOW_LOCAL_URLS);
        return validator.isValid(url);
    }

    public static boolean isInvalidUrlPath(String urlPath) {
        return !isValidUrlPath(urlPath);
    }

    public static boolean isValidUrlPath(String urlPath) {
        var validator = new UrlValidator();
        return validator.isValid(urlPath);
    }
}
