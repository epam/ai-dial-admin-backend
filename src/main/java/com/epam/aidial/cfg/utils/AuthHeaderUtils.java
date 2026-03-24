package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@UtilityClass
public class AuthHeaderUtils {
    public static Map<String, String> getAuthHeaders() {
        var token = AuthorizationTokenHolder.getToken();
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return Map.of("Authorization", "Bearer " + token);
    }
}