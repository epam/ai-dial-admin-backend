package com.epam.aidial.cfg.security;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthorizationTokenHolder {

    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    public static String getToken() {
        return tokenHolder.get();
    }

    public static void setToken(String token) {
        tokenHolder.set(token);
    }

    public static void clearToken() {
        tokenHolder.remove();
    }

}
