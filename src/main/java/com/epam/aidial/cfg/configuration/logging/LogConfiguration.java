package com.epam.aidial.cfg.configuration.logging;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.epam.aidial.cfg.configuration.logging.CustomizableTraceInterceptorProperties.MessageType.ENTER;
import static com.epam.aidial.cfg.configuration.logging.CustomizableTraceInterceptorProperties.MessageType.EXCEPTION;
import static com.epam.aidial.cfg.configuration.logging.CustomizableTraceInterceptorProperties.MessageType.EXIT;

@Configuration
@EnableConfigurationProperties(CustomizableTraceInterceptorProperties.class)
public class LogConfiguration {

    @ConditionalOnProperty("com.epam.aidial.cfg.configuration.customizable-trace-interceptor.enabled")
    @Bean
    public MethodInterceptor customizableTraceInterceptor(CustomizableTraceInterceptorProperties properties) {
        var messages = properties.getMessages();
        var interceptor = new CustomizableTraceInterceptor();
        interceptor.setHideProxyClassNames(true);
        interceptor.setUseDynamicLogger(true);
        interceptor.setEnterMessage(messages.get(ENTER));
        interceptor.setExitMessage(messages.get(EXIT));
        interceptor.setExceptionMessage(messages.get(EXCEPTION));
        return interceptor;
    }

    @ConditionalOnProperty("com.epam.aidial.cfg.configuration.customizable-trace-interceptor.enabled")
    @Bean
    public Advisor traceLogAdvisor(MethodInterceptor customizableTraceInterceptor,
                                   @Value("${com.epam.aidial.cfg.configuration.trace-log-advisor.expression}") String expression) {
        var aspectjExpressionPointcut = new AspectJExpressionPointcut();
        aspectjExpressionPointcut.setExpression(expression);
        return new DefaultPointcutAdvisor(aspectjExpressionPointcut, customizableTraceInterceptor);
    }
}
