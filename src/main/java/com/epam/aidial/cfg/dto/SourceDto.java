package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceDto {
    private SourceTypeDto type;
    private String name;
}