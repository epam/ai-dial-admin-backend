package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class ConfigRevisionDto {

    private Integer id;
    private Long timestamp;
    private String author;
    private String email;
}
