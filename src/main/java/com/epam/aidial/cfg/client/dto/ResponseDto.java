package com.epam.aidial.cfg.client.dto;

import com.epam.aidial.cfg.dto.validation.annotation.HttpStatusCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto {

    @HttpStatusCode
    private int status;
    private String body;

}
