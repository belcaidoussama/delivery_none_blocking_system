package com.system.delivery.producer;



import com.system.delivery.schema.DeliveryStatusUpdate;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service // Ensure this annotation is present
@RequiredArgsConstructor
public class DeliveryStatusProducerService {
    // Kafka properties injected
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    private static final String TOPIC = "delivery-status-updates";

    private Producer<String, DeliveryStatusUpdate> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);

        return new KafkaProducer<>(props);
    }

    public CompletableFuture<Void> sendStatusUpdate(DeliveryStatusUpdate event) {
        return CompletableFuture.runAsync(() -> {
            try (Producer<String, DeliveryStatusUpdate> producer = createProducer()) {
                ProducerRecord<String, DeliveryStatusUpdate> record =
                        new ProducerRecord<>(TOPIC,Long.toString( event.getBookingId()), event);
                producer.send(record);
                log.info("Kafka event sent: {}", event);
            }
        });
    }
}
