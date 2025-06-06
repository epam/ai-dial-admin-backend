package com.epam.aidial.cfg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportKeyInfo extends ExportComponentInfo {

    private List<String> roles;
    private Long expiresAt;
    private Long keyGeneratedAt;
}
