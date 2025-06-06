package com.epam.aidial.cfg.configuration.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.valves.AccessLogValve;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import java.io.CharArrayWriter;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty("server.tomcat.accesslog.enabled")
@ConditionalOnClass(name = {"org.apache.catalina.startup.Tomcat"})
public class TomcatFactoryCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final ServerProperties serverProperties;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        var pattern = serverProperties.getTomcat().getAccesslog().getPattern();
        var accessLogValve = new AccessLogValve() {
            @Override
            public void log(CharArrayWriter message) {
                log.info(message.toString());
            }

            @Override
            protected synchronized void open() {
                // do nothing
            }
        };
        accessLogValve.setEnabled(true);
        accessLogValve.setPattern(pattern);
        factory.addContextValves(accessLogValve);
    }
}
