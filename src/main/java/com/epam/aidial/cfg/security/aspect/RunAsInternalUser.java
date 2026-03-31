package com.epam.aidial.cfg.security.aspect;

import com.epam.aidial.cfg.security.InternalSecurityAuthenticationToken;
import com.epam.aidial.cfg.web.security.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAsInternalUser {

    String principal() default InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL;

    String email() default InternalSecurityAuthenticationToken.SYSTEM_PRINCIPAL_EMAIL;

    UserRole[] roles() default {UserRole.FULL_ADMIN};
}
