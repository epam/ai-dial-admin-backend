package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.web.security.SecurityPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource(properties = {
    "config.rest.security.mode=none",
})
@ComponentScan(basePackageClasses = {
    SecurityPackage.class,
})
public abstract class AbstractControllerNoneSecureTest {

    @Autowired
    protected MockMvc mockMvc;

}
