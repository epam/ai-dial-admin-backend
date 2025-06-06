package com.epam.aidial.cfg.dto;

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
public class ExportKeyInfoDto extends ExportComponentInfoDto {

    private List<String> roles;
    private Long expiresAt;
    private Long keyGeneratedAt;
}
