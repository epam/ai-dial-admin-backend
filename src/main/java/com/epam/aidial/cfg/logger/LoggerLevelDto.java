package com.epam.aidial.cfg.logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class LoggerLevelDto {

    private String defaultLevel;
    private String configuredLevel;
    private Long validTill;

}
