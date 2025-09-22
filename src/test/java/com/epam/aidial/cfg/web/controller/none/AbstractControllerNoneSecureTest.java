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
    protected static final String HEADER_ETAG = "eTag";
    protected static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    protected static final String HEADER_IF_MATCH = "If-Match";

    @Autowired
    protected MockMvc mockMvc;

}
