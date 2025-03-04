package com.system.delivery.streams.config;



import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class MetricsConfig {
    private final PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    @PostConstruct
    public void init() {
        Metrics.addRegistry(prometheusRegistry);
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return prometheusRegistry;
    }
}
