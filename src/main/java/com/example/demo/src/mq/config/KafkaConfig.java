package com.example.demo.src.mq.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Map;

@Configuration
public class KafkaConfig {

    private final KafkaProperties props;

    public KafkaConfig(KafkaProperties props) {
        this.props = props;
    }

    // ===== Producer (String key / byte[] value) =====
    @Bean
    public ProducerFactory<String, byte[]> producerFactory() {
        Map<String, Object> cfg = props.buildProducerProperties();
        cfg.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        cfg.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(cfg);
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ===== Consumer (String key / byte[] value) =====
    @Bean
    public ConsumerFactory<String, byte[]> consumerFactory() {
        Map<String, Object> cfg = props.buildConsumerProperties();
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean(name = "bytesManualAckListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> listenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(consumerFactory());
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // 視需要：f.setConcurrency(1);
        return f;
    }
}
