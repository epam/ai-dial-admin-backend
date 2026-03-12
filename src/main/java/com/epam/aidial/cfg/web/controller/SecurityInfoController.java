package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.SecurityInfoDto;
import com.epam.aidial.cfg.web.security.AdminRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/security-info")
@LogExecution
@RequiredArgsConstructor
public class SecurityInfoController {

    private static final Set<String> KNOWN_ADMIN_ROLES = Arrays.stream(AdminRole.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public SecurityInfoDto getSecurityInfo() {
        var dto = new SecurityInfoDto();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            var roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(KNOWN_ADMIN_ROLES::contains)
                    .collect(Collectors.toSet());
            dto.setRoles(roles);
        }
        return dto;
    }
}
