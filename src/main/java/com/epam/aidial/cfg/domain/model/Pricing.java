package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class Pricing {
    private String unit;
    private String prompt;
    private String completion;
}