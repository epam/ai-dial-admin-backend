package com.epam.aidial.cfg.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@ConditionalOnProperty(value = "config.rest.security.mode", havingValue = "oidc", matchIfMissing = true)
public class SecurityConfiguration {

    @Value("${config.rest.security.allowedRoles}")
    protected String[] allowedRoles;

    @Value("${config.rest.security.disable-swagger-authorization}")
    protected boolean disableSwaggerAuthorization;

    @Autowired
    private TokenDecoderFactory tokenDecoderFactory;

    @Autowired
    private JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(swaggerPathPatterns()).permitAll()
                    .requestMatchers("/api/v1/**").hasAnyAuthority(allowedRoles)
                    .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                    .jwt(jwt -> jwt.decoder(tokenDecoderFactory.createJwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverterFactory.create())));
        return http.build();
    }

    protected String[] swaggerPathPatterns() {
        return disableSwaggerAuthorization ? new String[]{"/swagger-ui/**", "/v3/api-docs/**"} : new String[] {};
    }
}
