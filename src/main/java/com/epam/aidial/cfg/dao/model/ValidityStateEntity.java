package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ValidityStateEntity {

    @Column(name = "validity_state_message")
    private String message;
    @Column(name = "validity_state_is_valid", nullable = false)
    private boolean isValid;
}