package org.example.springbootopentelemetrydemo;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * The OpenTelemetryAppender for both Logback and Log4j requires access to an OpenTelemetry instance to function properly
 * This instance must be set programmatically during application startup
 *
 * See Details: https://docs.spring.io/spring-boot/reference/actuator/loggers.html#actuator.loggers.opentelemetry
 */
@Component
class InstallOpenTelemetryAppender implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        OpenTelemetryAppender.install(this.openTelemetry);
    }

}
