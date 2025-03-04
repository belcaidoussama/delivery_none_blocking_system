package com.system.delivery.streams.config;


import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class KafkaStreamsStarter {

    private final StreamsBuilder streamsBuilder;
    private KafkaStreams kafkaStreams;

    public KafkaStreamsStarter(StreamsBuilder streamsBuilder) {
        this.streamsBuilder = streamsBuilder;
    }

      //  Inject from application.properties
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${spring.kafka.streams.state-dir}")
    private String stateDir;


    @EventListener(ApplicationReadyEvent.class)
    public void startKafkaStreams() {
        if (kafkaStreams == null) {
            Properties streamsConfig = new Properties();
            streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
            streamsConfig.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);

            kafkaStreams = new KafkaStreams(streamsBuilder.build(), streamsConfig);
            kafkaStreams.start();
            System.out.println(" Kafka Streams Started Successfully!");
        }
    }

}
