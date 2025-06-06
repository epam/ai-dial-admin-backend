package com.epam.aidial.cfg.logger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggerLevelsDto {

    private Map<String, LoggerLevelDto> loggerLevels;

}
