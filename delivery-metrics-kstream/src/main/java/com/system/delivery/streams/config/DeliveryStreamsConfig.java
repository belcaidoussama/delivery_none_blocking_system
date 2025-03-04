package com.system.delivery.streams.config;

import com.system.delivery.schema.DeliveryStatusUpdate;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Counter;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Consumed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableKafkaStreams
public class DeliveryStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${spring.kafka.streams.state-dir}")
    private String stateDir;

    @Value("${delivery.streams.status.topic}")
    private String statusTopic;

    private final MeterRegistry meterRegistry;
    private final Counter totalDeliveriesUpdates;
    private final Map<String, Counter> deliveriesPerStatus;
    public DeliveryStreamsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.totalDeliveriesUpdates = meterRegistry.counter("deliveries_total");
        this.deliveriesPerStatus = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        System.out.println(" Kafka Streams Application Started - Prometheus Metrics Enabled");
    }

    @Bean
    public StreamsConfig kafkaStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5000);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0); // Real-time updates
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class.getName()); //  Handle errors
        return new StreamsConfig(props);
    }


    // simple metrics just to showcase the worflow between api -> kafka -> kstream -> prometheus
    @Bean
    public KStream<String, DeliveryStatusUpdate> deliveryMetricsStream(StreamsBuilder builder) {
        System.out.println(" Initializing KStream for topic: " + statusTopic);

        SpecificAvroSerde<DeliveryStatusUpdate> deliveryStatusSerde = new SpecificAvroSerde<>();
        Map<String, String> serdeConfig = Map.of("schema.registry.url", schemaRegistryUrl);
        deliveryStatusSerde.configure(serdeConfig, false);

        KStream<String, DeliveryStatusUpdate> stream = builder.stream(
                statusTopic, Consumed.with(Serdes.String(), deliveryStatusSerde)
        );


        stream.foreach((key, value) -> {
            // Track total deliveries updates
            totalDeliveriesUpdates.increment();
            // Track deliveries per status
            deliveriesPerStatus
                    .computeIfAbsent(value.getStatus().toString(),
                            status -> meterRegistry.counter("deliveries_per_status", "status", status))
                    .increment();
        });

        //we can add more metrics like avg delivery time , delayed deliveries ...

        return stream;
    }
}
