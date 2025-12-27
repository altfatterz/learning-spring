# Spring Boot 4 with OpenTelemetry


### Docker OpenTelemetry (OTel) LGTM - https://grafana.com/docs/opentelemetry/docker-lgtm/#docker-opentelemetry-lgtm

- The LGTM stack is a modern, open-source observability suite developed by Grafana Labs. 
- It is designed to solve the "three pillars of observability" (`Logs`, `Metrics`, and `Traces`) in a unified way.

- Before LGTM, engineers often had to jump between different tools (like `Elasticsearch` for logs, `Prometheus` for metrics, and `Jaeger` for traces)
- LGTM brings these into a single interface: Grafana

- Four components - Each letter in `LGTM` represents a specific tool:
  - `Loki`	
    - `Logs aggregation. 
    - It indexes metadata (labels) rather than the full text, making it cost-effective and fast.	
    - The `Event Log` that tells you exactly `what` happened.
  - `Grafana`
    - Visualization & Alerting. 
    - It is the "brain" and UI that connects to the other three.	
    - The `Dashboard` or single pane of glass.
  - `Tempo` 
    - Traces. 
    - A high-scale backend for distributed tracing to follow a request across services.	
    - The `Map` that shows where a request traveled and where it got stuck.
  - `Mimir`	
    - Metrics. 
    - A horizontally scalable long-term storage for Prometheus-compatible metrics.	
    - The `Vitals` (CPU, Memory, Error Rates) that tell you if there is a problem.

### How data flows
  - `Your App`: Generates telemetry (Logs, Metrics, Traces) via the OpenTelemetry SDK
  - `OTel Collector`: Receives that data, processes it (removes sensitive info, adds labels), and sends it to the backends
  - `Backends`: `Loki` (Logs), `Tempo` (Traces), and `Mimir` (Metrics) store the data
  - `Grafana`: Queries all three backends to show you everything in one dashboard.


### OpenTelemetry for Logs

- By default, logging via OpenTelemetry is not configured. You have to provide the location of the OpenTelemetry logs endpoint to configure it

```bash
management:
  opentelemetry:
    logging:
      export:
        otlp:
          endpoint: "https://otlp.example.com:4318/v1/logs"
```

- These appenders are not part of Spring Boot, you have to configure the appender in your `logback-spring.xml` or `log4j2-spring.xml` 
configuration to get OpenTelemetry logging working

- [`OpenTelemetry Logback appender`](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/logback/logback-appender-1.0/library)
  - This module provides a Logback appender which forwards Logback log events to the [OpenTelemetry Log SDK](https://github.com/open-telemetry/opentelemetry-java).
- [`OpenTelemetry Log4j appender`](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/log4j/log4j-appender-2.17/library)
  - This module provides a Log4j2 appender which forwards Log4j2 log events to the [OpenTelemetry Log SDK](https://github.com/open-telemetry/opentelemetry-java).



### Start LGTM stack

```bash
$ docker compose up

grafana-lgtm-1  | The OpenTelemetry collector and the Grafana LGTM stack are up and running. (created /tmp/ready)
grafana-lgtm-1  | Open ports:
grafana-lgtm-1  |  - 4317: OpenTelemetry GRPC endpoint
grafana-lgtm-1  |  - 4318: OpenTelemetry HTTP endpoint
grafana-lgtm-1  |  - 3000: Grafana (http://localhost:3000). User: admin, password: admin
grafana-lgtm-1  |  - 4040: Pyroscope endpoint
grafana-lgtm-1  |  - 9090: Prometheus endpoint
```

### Application logs

```bash
2025-12-27T10:40:17.178+01:00  INFO 93555 --- [spring-boot-otel-demo] [nio-8080-exec-3] [b0869c3ec385bdc78d8000d5cd51d507-7eee2aafc067b83a] o.e.s.HomeController                     : Home endpoint called
```

- TraceId: identifies the entire request flow across all services, 
- SpanId: 

Use-cases:

- `jump from trace to logs`:
  - view the trace in Tempo and click on the span to see only the logs emitted during that exact operation
- `jump from logs to trace`
  - if error is found in the logs, use the traceId in Tempo to see the full request flow




### Resources
- OpenTelemetry with Spring Boot 4: https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot
- Observability https://docs.spring.io/spring-boot/reference/actuator/observability.html