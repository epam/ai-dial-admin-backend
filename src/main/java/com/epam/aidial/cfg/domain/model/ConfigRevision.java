package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class ConfigRevision {

    private Integer id;
    private Long timestamp;
    private String author;
    private String email;
}
