package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.dto.AdapterDto;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "config")
public class AdaptersConfigProperties {

    private List<AdapterDto> adapters;
}
