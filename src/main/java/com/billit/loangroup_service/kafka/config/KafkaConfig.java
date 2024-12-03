package com.billit.loangroup_service.kafka.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkKafkaConfig() {
        log.info("Kafka bootstrap servers: {}",
                environment.getProperty("spring.kafka.bootstrap-servers"));
    }

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // Producer 설정
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, LoanGroupPartitioner.class.getName());
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Consumer 설정
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }

    // Topics
    @Bean
    public NewTopic settlementCalculationTopic() {
        return TopicBuilder.name("settlement-calculation")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic loanDisbursementTopic() {
        return TopicBuilder.name("loan-disbursement")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic loanStatusUpdateTopic() {
        return TopicBuilder.name("loan-status-update")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic investmentDateUpdateTopic() {
        return TopicBuilder.name("investment-date-update")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic repaymentScheduleTopic() {
        return TopicBuilder.name("repayment-schedule")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic excessRefundTopic() {
        return TopicBuilder.name("excess-refund")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Error Handler
    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(
                (consumerRecord, exception) -> {
                    log.error("Error in process with Exception {} and the record is {}",
                            exception, consumerRecord);
                },
                new FixedBackOff(3000L, 3)
        );
    }
}