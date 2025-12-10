package com.epam.aidial.cfg.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class TokenResponseDto {

    @JsonAlias({"access_token", "accessToken"})
    private String accessToken;
    @JsonAlias({"expires_in", "expiresIn"})
    private int expiresIn;

}
