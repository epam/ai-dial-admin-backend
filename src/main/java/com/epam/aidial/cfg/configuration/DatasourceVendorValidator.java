package com.epam.aidial.cfg.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Set;

@Slf4j
public class DatasourceVendorValidator implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Set<String> VALID_VENDORS = Set.of("H2", "POSTGRES", "MS_SQL_SERVER");
    private static final String DATASOURCE_VENDOR_PROPERTY = "datasource.vendor";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment environment = event.getEnvironment();
        String vendor = environment.getProperty(DATASOURCE_VENDOR_PROPERTY);

        log.info("Validating datasource.vendor property. Value: {}", vendor);

        if (StringUtils.isBlank(vendor)) {
            throw new IllegalStateException("Undefined datasource.vendor value: '" + vendor
                    + "'. " + "Valid values are: " + String.join(", ", VALID_VENDORS));
        }

        if (!VALID_VENDORS.contains(vendor)) {
            throw new IllegalStateException("Invalid datasource.vendor value: '" + vendor + "'. "
                    + "Valid values are: " + String.join(", ", VALID_VENDORS));
        }

        log.info("Datasource vendor validation successful for vendor: {}", vendor);
    }
}