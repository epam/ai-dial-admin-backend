package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.Set;

@Data
public class SecurityInfoDto {

    private Set<String> roles;
}
