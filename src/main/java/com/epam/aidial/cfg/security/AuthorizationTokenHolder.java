package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.utils.SecretUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class AuthorizationTokenHolder {

    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    public static String getToken() {
        var token = tokenHolder.get();

        if (log.isTraceEnabled()) {
            log.trace("getToken. token: {}", token);
        } else {
            log.debug("getToken. token: {}", SecretUtils.mask(token));
        }

        return token;
    }

    public static void setToken(String token) {
        if (log.isTraceEnabled()) {
            log.trace("setToken. token: {}", token);
        } else {
            log.debug("setToken. token: {}", SecretUtils.mask(token));
        }
        tokenHolder.set(token);
    }

    public static void clearToken() {
        log.debug("clearToken");
        tokenHolder.remove();
    }

}
