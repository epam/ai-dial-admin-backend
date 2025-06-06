package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ResponseEntity {

    private int status;
    private String body;
}
