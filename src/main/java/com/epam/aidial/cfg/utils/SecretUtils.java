package com.epam.aidial.cfg.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public final class SecretUtils {

    public static String mask(String value) {
        if (value == null) {
            return null;
        }
        String result = (StringUtils.length(value) > 15) ? value.substring(0, 6) + "*******" : "**********";
        result += ". (hash: " + value.hashCode() + ")";
        return result;
    }
}
