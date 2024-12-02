package com.billit.loangroup_service.kafka.config;

import com.billit.loangroup_service.event.domain.LoanGroupFullEvent;
import com.billit.loangroup_service.event.domain.LoanGroupInvestmentCompleteEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public NewTopic loanGroupFullTopic() {
        return TopicBuilder.name("loan-group-full")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic investmentCompleteTopic() {
        return TopicBuilder.name("investment-complete")
                .partitions(1)
                .replicas(1)
                .build();
    }

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

    @Bean
    public ConsumerFactory<String, LoanGroupFullEvent> loanGroupFullEventConsumerFactory() {
        JsonDeserializer<LoanGroupFullEvent> deserializer = new JsonDeserializer<>(LoanGroupFullEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConsumerFactory<String, LoanGroupInvestmentCompleteEvent> investmentCompleteEventConsumerFactory() {
        JsonDeserializer<LoanGroupInvestmentCompleteEvent> deserializer =
                new JsonDeserializer<>(LoanGroupInvestmentCompleteEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LoanGroupFullEvent>
    loanGroupFullEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, LoanGroupFullEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(loanGroupFullEventConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LoanGroupInvestmentCompleteEvent>
    investmentCompleteEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, LoanGroupInvestmentCompleteEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(investmentCompleteEventConsumerFactory());
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(
                (consumerRecord, exception) -> {
                    System.err.println("Error in process with Exception {} and the record is {}"
                            + exception + consumerRecord);
                },
                new FixedBackOff(3000L, 3)
        );
    }
}