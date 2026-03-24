package com.epam.aidial.cfg.security.aspect;

import com.epam.aidial.cfg.security.SystemAuthenticationToken;
import com.epam.aidial.cfg.web.security.UserRole;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAsSystemUser {

    @AliasFor("username")
    String value() default SystemAuthenticationToken.SYSTEM_PRINCIPAL;

    @AliasFor("value")
    String username() default SystemAuthenticationToken.SYSTEM_PRINCIPAL;

    UserRole[] roles() default {UserRole.FULL_ADMIN};
}
