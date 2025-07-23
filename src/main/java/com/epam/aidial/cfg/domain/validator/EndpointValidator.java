package com.epam.aidial.cfg.domain.validator;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isEmpty(urlPath)) {
            return true;
        }

        String path = urlPath;
        if (!path.isEmpty() && !path.startsWith("/")) {
            path = "/" + path;
        }

        return path.matches("^/?[\\w\\-./\\?=;]*$");
    }
}
