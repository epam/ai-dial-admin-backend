package com.epam.aidial.cfg.configuration;

import com.epam.aidial.metric.MetricPackage;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {
        MetricPackage.class
})
public class MetricConfiguration {
}
