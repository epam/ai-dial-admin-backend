package com.epam.aidial.cfg.configuration;

import com.epam.aidial.metric.MetricPackage;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!cli")
@ComponentScan(basePackageClasses = {
        MetricPackage.class
})
public class MetricConfiguration {
}
