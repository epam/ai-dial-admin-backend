package com.epam.aidial.cfg.security;

import java.io.Closeable;


public class AuthorizationTokenWrapper implements Closeable {

    public AuthorizationTokenWrapper(String token) {
        AuthorizationTokenHolder.setToken(token);
    }

    @Override
    public void close() {
        AuthorizationTokenHolder.clearToken();
    }

}
