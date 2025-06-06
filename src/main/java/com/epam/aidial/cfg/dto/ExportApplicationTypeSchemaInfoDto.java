package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportApplicationTypeSchemaInfoDto extends ExportComponentInfoDto {

    @JsonProperty("$id")
    private String id;

    @JsonProperty("dial:applicationTypeDisplayName")
    @Override
    public String getDisplayName() {
        return super.getDisplayName();
    }

    @JsonProperty("dial:applicationTypeDisplayName")
    @Override
    public void setDisplayName(String displayName) {
        super.setDisplayName(displayName);
    }
}
