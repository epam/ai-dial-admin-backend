package com.epam.aidial.cfg.client.dto;

import lombok.Data;

@Data
public class TokenResponseDto {

    private String accessToken;
    private String tokenType;
    private int expiresIn;
}
