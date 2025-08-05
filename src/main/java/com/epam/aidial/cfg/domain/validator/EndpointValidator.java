package com.epam.aidial.cfg.domain.validator;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import static org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS;

import java.util.regex.Pattern;

@UtilityClass
public class EndpointValidator {

    private static final Pattern VALID_URL_PATTERN = Pattern.compile("^[a-zA-Z0-9-.:/\\\\]+$");

    public static boolean isInvalidUrl(String url) {
        return !isValidUrl(url);
    }

    public static boolean isValidUrl(String url) {
        if (!VALID_URL_PATTERN.matcher(url).matches()) {
            return false;
        }
        String[] schemes = {"http", "https"};
        var validator = new CustomUrlValidator(schemes, ALLOW_LOCAL_URLS);
        return validator.isValid(url);
    }

    public static boolean isInvalidUrlPath(String urlPath) {
        return !isValidUrlPath(urlPath);
    }

    public static boolean isValidUrlPath(String urlPath) {
        if (StringUtils.isEmpty(urlPath)) {
            return false;
        }

        String path = urlPath;
        if (!path.isEmpty() && !path.startsWith("/")) {
            path = "/" + path;
        }

        return path.matches("^/?[\\w\\-./]*$");
    }

    private class CustomUrlValidator extends UrlValidator {

        private static final Pattern VALID_AUTHORITY_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+(:[0-9]{1,5})?$");

        public CustomUrlValidator(String[] schemes, long options) {
            super(schemes, options);
        }

        @Override
        protected boolean isValidAuthority(String authority) {
            return authority != null && VALID_AUTHORITY_PATTERN.matcher(authority).matches();
        }
    }
}
