package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class PricingEntity {
    private String unit;
    private String prompt;
    private String completion;
}