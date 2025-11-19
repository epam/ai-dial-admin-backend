package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.exception.InvalidDatasourceVendorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class DatasourceVendorProperties {

    private static final Set<String> VALID_VENDORS = Set.of("H2", "POSTGRES", "MS_SQL_SERVER");

    @Value("${datasource.vendor}")
    private String vendor;

    @PostConstruct
    public void validateDatasourceVendor() {
        log.info("Validating datasource.vendor property. Value: {}", vendor);

        if (StringUtils.isBlank(vendor)) {
            throw new InvalidDatasourceVendorException("Undefined datasource.vendor value: '" + vendor
                    + "'. " + "Valid values are: " + String.join(", ", VALID_VENDORS));
        }

        if (!VALID_VENDORS.contains(vendor)) {
            throw new InvalidDatasourceVendorException("Invalid datasource.vendor value: '" + vendor + "'. "
                    + "Valid values are: " + String.join(", ", VALID_VENDORS));
        }

        log.info("Datasource vendor validation successful for vendor: {}", vendor);
    }
}

